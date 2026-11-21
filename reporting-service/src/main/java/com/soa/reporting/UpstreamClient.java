package com.soa.reporting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Component public class UpstreamClient {
 @Value("${order.service.url}") private String ordersUrl;
 @Value("${product.service.url}") private String productsUrl;
 private final RestTemplate http=new RestTemplate();
 public record Item(Integer id,Integer orderId,Integer productId,String productName,Integer quantity,BigDecimal unitPrice,BigDecimal totalPrice){}
 public record Order(Integer id,String customerName,String customerEmail,BigDecimal totalAmount,String status,LocalDateTime createdAt,LocalDateTime updatedAt,List<Item> items){}
 public record Product(Integer id,String name,BigDecimal price,Integer quantity,BigDecimal costPrice){}
 private <T> T get(String url,String bearer,Class<T> type){
  HttpHeaders headers=new HttpHeaders();headers.set("Authorization",bearer);
  try{T result=http.exchange(url,HttpMethod.GET,new HttpEntity<>(headers),type).getBody();
   if(result==null)throw new ApiException(HttpStatus.BAD_GATEWAY,"Upstream returned empty response");return result;
  }catch(HttpClientErrorException.NotFound e){throw new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy dữ liệu tại service nguồn");}
   catch(HttpClientErrorException.Unauthorized e){throw new ApiException(HttpStatus.UNAUTHORIZED,"Token bị service nguồn từ chối");}
   catch(HttpClientErrorException.Forbidden e){throw new ApiException(HttpStatus.FORBIDDEN,"Service nguồn từ chối quyền truy cập");}
   catch(HttpClientErrorException e){throw new ApiException(HttpStatus.BAD_GATEWAY,"Service nguồn trả HTTP "+e.getStatusCode().value());}
   catch(RestClientException e){throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"Không truy cập được service nguồn");}
 }
 public Order order(Integer id,String bearer){return get(ordersUrl+"/orders/"+id,bearer,Order.class);}
 public Product product(Integer id,String bearer){return get(productsUrl+"/products/"+id,bearer,Product.class);}
}
