package com.soa.reporting;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
@Service public class ReportService {
 private final OrderReportRepository orders; private final ProductReportRepository products;private final UpstreamClient upstream;
 public ReportService(OrderReportRepository orders,ProductReportRepository products,UpstreamClient upstream){this.orders=orders;this.products=products;this.upstream=upstream;}
 public record CreateOrderInput(Integer orderId){}
 public record CreateProductInput(Integer orderId,Integer productId){}
 public record OrderView(Integer id,Integer orderId,BigDecimal totalRevenue,BigDecimal totalCost,BigDecimal totalProfit,List<ProductView> products){}
 public record ProductView(Integer id,Integer orderReportId,Integer productId,Integer totalSold,BigDecimal revenue,BigDecimal cost,BigDecimal profit,Integer stockQuantity){}
 private ApiException bad(String message){return new ApiException(HttpStatus.BAD_REQUEST,message);}
 private OrderReport order(Integer id){return orders.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy báo cáo đơn hàng"));}
 private ProductReport product(Integer id){return products.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy báo cáo sản phẩm"));}
 private ProductView view(ProductReport p){return new ProductView(p.id,p.orderReportId,p.productId,p.totalSold,p.revenue,p.cost,p.profit,p.stockQuantity);}
 private OrderView view(OrderReport o){return new OrderView(o.id,o.orderId,o.totalRevenue,o.totalCost,o.totalProfit,products.findByOrderReportId(o.id).stream().map(this::view).toList());}
 @Transactional(readOnly=true) public List<OrderView> listOrders(){return orders.findAll().stream().map(this::view).toList();}
 @Transactional(readOnly=true) public OrderView getOrder(Integer id){return view(order(id));}
 @Transactional(readOnly=true) public List<ProductView> listProducts(){return products.findAll().stream().map(this::view).toList();}
 @Transactional(readOnly=true) public ProductView getProduct(Integer id){return view(product(id));}
 private List<ProductReport> prepared(Integer reportId,UpstreamClient.Order order,String bearer){
  if(order.id()==null||!"completed".equalsIgnoreCase(order.status()))throw new ApiException(HttpStatus.CONFLICT,"Chỉ báo cáo đơn hàng completed");
  if(order.items()==null||order.items().isEmpty())throw bad("Đơn hàng không có sản phẩm");
  Map<Integer,ProductReport> result=new LinkedHashMap<>();
  for(UpstreamClient.Item i:order.items()){
   if(i.productId()==null||i.quantity()==null||i.quantity()<=0||i.unitPrice()==null||i.unitPrice().signum()<0)throw new ApiException(HttpStatus.BAD_GATEWAY,"Chi tiết đơn hàng không hợp lệ");
   UpstreamClient.Product p=upstream.product(i.productId(),bearer);
   // Cost price must come from Product Service, NEVER invented or inferred from sale price.
   if(p.costPrice()==null||p.costPrice().signum()<0)throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,"Sản phẩm "+i.productId()+" chưa có costPrice hợp lệ");
   BigDecimal revenue=i.unitPrice().multiply(BigDecimal.valueOf(i.quantity()));
   BigDecimal cost=p.costPrice().multiply(BigDecimal.valueOf(i.quantity()));
   ProductReport current=result.get(i.productId());
   if(current==null){current=new ProductReport();current.orderReportId=reportId;current.productId=i.productId();current.totalSold=0;current.revenue=BigDecimal.ZERO;current.cost=BigDecimal.ZERO;current.stockQuantity=p.quantity();result.put(i.productId(),current);}
   current.totalSold=Math.addExact(current.totalSold,i.quantity());current.revenue=current.revenue.add(revenue);current.cost=current.cost.add(cost);current.profit=current.revenue.subtract(current.cost);
  }
  return new ArrayList<>(result.values());
 }
 @Transactional public OrderView createOrder(Integer orderId,String bearer){
  if(orderId==null||orderId<=0)throw bad("orderId là bắt buộc");
  if(orders.findByOrderId(orderId).isPresent())throw new ApiException(HttpStatus.CONFLICT,"Đã có báo cáo cho đơn hàng này");
  UpstreamClient.Order source=upstream.order(orderId,bearer);
  // Fetch and validate all data before writing anything.
  List<ProductReport> prepared=prepared(null,source,bearer);
  BigDecimal revenue=prepared.stream().map(p->p.revenue).reduce(BigDecimal.ZERO,BigDecimal::add);
  BigDecimal cost=prepared.stream().map(p->p.cost).reduce(BigDecimal.ZERO,BigDecimal::add);
  if(source.totalAmount()==null||source.totalAmount().compareTo(revenue)!=0)throw new ApiException(HttpStatus.CONFLICT,"Doanh thu mặt hàng không khớp tổng đơn hàng");
  OrderReport report=new OrderReport();report.orderId=orderId;report.totalRevenue=revenue;report.totalCost=cost;report.totalProfit=revenue.subtract(cost);
  orders.saveAndFlush(report);
  for(ProductReport p:prepared){p.orderReportId=report.id;products.save(p);}
  return view(report);
 }
 @Transactional public ProductView createProduct(Integer orderId,Integer productId,String bearer){
  if(orderId==null||orderId<=0||productId==null||productId<=0)throw bad("orderId và productId là bắt buộc");
  OrderReport report=orders.findByOrderId(orderId).orElse(null);
  if(report==null){
   createOrder(orderId,bearer);
   report=orders.findByOrderId(orderId).orElseThrow();
  }
  Integer reportId=report.id;
  return products.findByOrderReportIdAndProductId(reportId,productId)
   .map(this::view).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Sản phẩm không có trong đơn hàng"));
 }
 @Transactional public void deleteOrder(Integer id){order(id);products.deleteByOrderReportId(id);orders.deleteById(id);}
 @Transactional public void deleteProduct(Integer id){
  ProductReport item=product(id);
  OrderReport report=order(item.orderReportId);
  // Do not leave parent totals inconsistent: remove entire snapshot when its item is deleted.
  if(products.findByOrderReportId(report.id).size()==1){products.delete(item);orders.delete(report);return;}
  products.delete(item);
  report.totalRevenue=report.totalRevenue.subtract(item.revenue);
  report.totalCost=report.totalCost.subtract(item.cost);
  report.totalProfit=report.totalRevenue.subtract(report.totalCost);
  orders.save(report);
 }
}
