package com.soa.order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController public class OrderController{
 private final OrderService service;
 public OrderController(OrderService service){this.service=service;}
 @GetMapping("/orders") public List<OrderService.OrderResponse> list(){return service.all();}
 @GetMapping("/orders/{id}") public OrderService.OrderResponse find(@PathVariable Integer id){return service.find(id);}
 @PostMapping("/orders") public ResponseEntity<OrderService.OrderResponse> create(@RequestBody OrderService.CreateInput body,@RequestHeader("Authorization") String auth){return ResponseEntity.status(201).body(service.create(body,auth));}
 @PutMapping("/orders/{id}") public OrderService.OrderResponse status(@PathVariable Integer id,@RequestBody OrderService.StatusInput body){return service.changeStatus(id,body);}
 @DeleteMapping("/orders/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id){service.delete(id);return ResponseEntity.noContent().build();}
 @GetMapping("/order_items") public List<OrderService.ItemResponse> items(){return service.allItems();}
 @GetMapping("/order_items/{id}") public OrderService.ItemResponse item(@PathVariable Integer id){return service.findItem(id);}
 @PostMapping("/order_items") public ResponseEntity<OrderService.ItemResponse> add(@RequestBody OrderService.AddItemInput body,@RequestHeader("Authorization") String auth){return ResponseEntity.status(201).body(service.add(body,auth));}
 @PutMapping("/order_items/{id}") public OrderService.ItemResponse update(@PathVariable Integer id,@RequestBody OrderService.ItemInput body,@RequestHeader("Authorization") String auth){return service.updateItem(id,body,auth);}
 @DeleteMapping("/order_items/{id}") public ResponseEntity<Void> deleteItem(@PathVariable Integer id){service.deleteItem(id);return ResponseEntity.noContent().build();}
}
