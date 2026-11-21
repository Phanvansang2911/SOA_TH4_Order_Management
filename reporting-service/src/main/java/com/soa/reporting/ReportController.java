package com.soa.reporting;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/reports") public class ReportController {
 private final ReportService service;
 public ReportController(ReportService service){this.service=service;}
 @GetMapping("/orders") public List<ReportService.OrderView> orders(){return service.listOrders();}
 @GetMapping("/orders/{id}") public ReportService.OrderView order(@PathVariable Integer id){return service.getOrder(id);}
 @PostMapping("/orders") public ResponseEntity<ReportService.OrderView> createOrder(@RequestBody ReportService.CreateOrderInput input,@RequestHeader("Authorization") String bearer){return ResponseEntity.status(201).body(service.createOrder(input==null?null:input.orderId(),bearer));}
 @DeleteMapping("/orders/{id}") public ResponseEntity<Void> deleteOrder(@PathVariable Integer id){service.deleteOrder(id);return ResponseEntity.noContent().build();}
 @GetMapping("/products") public List<ReportService.ProductView> products(){return service.listProducts();}
 @GetMapping("/products/{id}") public ReportService.ProductView product(@PathVariable Integer id){return service.getProduct(id);}
 @PostMapping("/products") public ResponseEntity<ReportService.ProductView> createProduct(@RequestBody ReportService.CreateProductInput input,@RequestHeader("Authorization") String bearer){return ResponseEntity.status(201).body(service.createProduct(input==null?null:input.orderId(),input==null?null:input.productId(),bearer));}
 @DeleteMapping("/products/{id}") public ResponseEntity<Void> deleteProduct(@PathVariable Integer id){service.deleteProduct(id);return ResponseEntity.noContent().build();}
}
