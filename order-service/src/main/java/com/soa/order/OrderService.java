package com.soa.order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
@Service public class OrderService{
 private final OrderRepository orders;private final ItemRepository items;private final ProductClient products;
 public OrderService(OrderRepository orders,ItemRepository items,ProductClient products){this.orders=orders;this.items=items;this.products=products;}
 public record ItemInput(Integer productId,Integer quantity){}
 public record CreateInput(String customerName,String customerEmail,List<ItemInput> items){}
 public record StatusInput(String status){}
 public record AddItemInput(Integer orderId,Integer productId,Integer quantity){}
 public record ItemResponse(Integer id,Integer orderId,Integer productId,String productName,Integer quantity,BigDecimal unitPrice,BigDecimal totalPrice){}
 public record OrderResponse(Integer id,String customerName,String customerEmail,BigDecimal totalAmount,String status,java.time.LocalDateTime createdAt,java.time.LocalDateTime updatedAt,List<ItemResponse> items){}
 private ApiException bad(String msg){return new ApiException(HttpStatus.BAD_REQUEST,msg);}
 private OrderEntity get(Integer id){return orders.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy đơn hàng"));}
 private OrderItem getItem(Integer id){return items.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy mặt hàng"));}
 private void pending(OrderEntity o){if(!"pending".equals(o.status))throw new ApiException(HttpStatus.CONFLICT,"Chỉ sửa được đơn hàng pending");}
 private void valid(Integer id,Integer q){if(id==null||q==null||q<=0)throw bad("productId và quantity > 0 là bắt buộc");}
 private OrderItem make(Integer orderId,Integer id,Integer q,String auth){
  valid(id,q);ProductClient.Product p=products.lookup(id,auth);
  if(p.quantity()<q)throw new ApiException(HttpStatus.CONFLICT,"Không đủ tồn kho cho sản phẩm "+id);
  if(p.price().signum()<0)throw bad("Giá sản phẩm không hợp lệ");
  OrderItem item=new OrderItem();item.orderId=orderId;item.productId=id;item.productName=p.name();item.quantity=q;item.unitPrice=p.price();item.totalPrice=p.price().multiply(BigDecimal.valueOf(q));return item;
 }
 private ItemResponse itemResponse(OrderItem i){return new ItemResponse(i.id,i.orderId,i.productId,i.productName,i.quantity,i.unitPrice,i.totalPrice);}
 private OrderResponse response(OrderEntity o){return new OrderResponse(o.id,o.customerName,o.customerEmail,o.totalAmount,o.status,o.createdAt,o.updatedAt,items.findByOrderId(o.id).stream().map(this::itemResponse).toList());}
 @Transactional(readOnly=true) public List<OrderResponse> all(){return orders.findAll().stream().map(this::response).toList();}
 @Transactional(readOnly=true) public OrderResponse find(Integer id){return response(get(id));}
 @Transactional public OrderResponse create(CreateInput in,String auth){
  if(in==null||in.customerName()==null||in.customerName().isBlank()||in.customerEmail()==null||!in.customerEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")||in.items()==null||in.items().isEmpty())throw bad("Thông tin khách hàng và danh sách sản phẩm là bắt buộc");
  Map<Integer,Integer> counts=new LinkedHashMap<>();
  for(ItemInput x:in.items()){
   if(x==null){throw bad("Mặt hàng không hợp lệ");}valid(x.productId(),x.quantity());
   try{counts.merge(x.productId(),x.quantity(),(left,right)->Math.addExact(left,right));}catch(ArithmeticException e){throw bad("Số lượng quá lớn");}
  }
  // Verify ALL product quantities before creating any records.
  List<OrderItem> prepared=new ArrayList<>();
  for(var entry:counts.entrySet())prepared.add(make(null,entry.getKey(),entry.getValue(),auth));
  OrderEntity order=new OrderEntity();order.customerName=in.customerName().trim();order.customerEmail=in.customerEmail().trim();
  order.totalAmount=prepared.stream().map(i->i.totalPrice).reduce(BigDecimal.ZERO,BigDecimal::add);
  orders.save(order);
  for(OrderItem item:prepared){item.orderId=order.id;items.save(item);}
  return response(order);
 }
 @Transactional public OrderResponse changeStatus(Integer id,StatusInput body){
  OrderEntity o=get(id); if(body==null||body.status()==null||!Set.of("pending","completed","cancelled").contains(body.status()))throw bad("Status phải là pending, completed hoặc cancelled");
  if(!"pending".equals(o.status)&&!o.status.equals(body.status()))throw new ApiException(HttpStatus.CONFLICT,"Đơn hàng đã kết thúc");
  o.status=body.status();orders.saveAndFlush(o);return response(o);
 }
 @Transactional public void delete(Integer id){get(id);items.deleteByOrderId(id);orders.deleteById(id);}
 @Transactional(readOnly=true) public List<ItemResponse> allItems(){return items.findAll().stream().map(this::itemResponse).toList();}
 @Transactional(readOnly=true) public ItemResponse findItem(Integer id){return itemResponse(getItem(id));}
 private void recalc(OrderEntity o){o.totalAmount=items.findByOrderId(o.id).stream().map(i->i.totalPrice).reduce(BigDecimal.ZERO,BigDecimal::add);orders.saveAndFlush(o);}
 @Transactional public ItemResponse add(AddItemInput in,String auth){
  if(in==null||in.orderId()==null)throw bad("orderId là bắt buộc");OrderEntity o=get(in.orderId());pending(o);
  // Do not let repeated product lines exceed stock in aggregate.
  int existing=items.findByOrderId(o.id).stream().filter(i->i.productId.equals(in.productId())).mapToInt(i->i.quantity).sum();
  OrderItem i=make(o.id,in.productId(),in.quantity(),auth);
  ProductClient.Product p=products.lookup(in.productId(),auth);
  if((long)existing+in.quantity()>p.quantity())throw new ApiException(HttpStatus.CONFLICT,"Tổng số lượng vượt tồn kho");
  items.save(i);recalc(o);return itemResponse(i);
 }
 @Transactional public ItemResponse updateItem(Integer id,ItemInput in,String auth){
  OrderItem old=getItem(id);OrderEntity o=get(old.orderId);pending(o);
  if(in==null)throw bad("Thiếu mặt hàng");
  long other=items.findByOrderId(o.id).stream().filter(i->!i.id.equals(id)&&i.productId.equals(in.productId())).mapToLong(i->i.quantity).sum();
  OrderItem updated=make(o.id,in.productId(),in.quantity(),auth);
  ProductClient.Product p=products.lookup(in.productId(),auth);
  if(other+in.quantity()>p.quantity())throw new ApiException(HttpStatus.CONFLICT,"Tổng số lượng vượt tồn kho");
  old.productId=updated.productId;old.productName=updated.productName;old.quantity=updated.quantity;old.unitPrice=updated.unitPrice;old.totalPrice=updated.totalPrice;
  items.save(old);recalc(o);return itemResponse(old);
 }
 @Transactional public void deleteItem(Integer id){OrderItem item=getItem(id);OrderEntity o=get(item.orderId);pending(o);items.delete(item);items.flush();recalc(o);}
}
