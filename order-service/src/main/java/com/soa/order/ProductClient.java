package com.soa.order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.math.BigDecimal;
@Component public class ProductClient{
 @Value("${product.service.url}") String url;
 private final RestTemplate http=new RestTemplate();
 public record Product(Integer id,String name,BigDecimal price,Integer quantity){}
 public Product lookup(Integer id,String bearer){
  try{HttpHeaders h=new HttpHeaders(); h.set("Authorization",bearer);
   Product p=http.exchange(url+"/products/"+id,HttpMethod.GET,new HttpEntity<>(h),Product.class).getBody();
   if(p==null||p.id()==null||p.name()==null||p.price()==null||p.quantity()==null)throw new ApiException(HttpStatus.BAD_GATEWAY,"Product Service trả dữ liệu không hợp lệ");
   return p;
  }catch(HttpClientErrorException.NotFound e){throw new ApiException(HttpStatus.BAD_REQUEST,"Không tìm thấy sản phẩm "+id);}
   catch(HttpClientErrorException.Unauthorized e){throw new ApiException(HttpStatus.UNAUTHORIZED,"Token bị Product Service từ chối");}
   catch(RestClientException e){throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"Product Service không khả dụng");}
 }
}
