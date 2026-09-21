package com.rishikesan.retailanalyticsengine.ingestion;

import com.rishikesan.retailanalyticsengine.model.Customer;
import com.rishikesan.retailanalyticsengine.model.Order;
import com.rishikesan.retailanalyticsengine.model.OrderItem;
import com.rishikesan.retailanalyticsengine.model.Product;
import com.rishikesan.retailanalyticsengine.repository.CustomerRepository;
import com.rishikesan.retailanalyticsengine.repository.OrderItemRepository;
import com.rishikesan.retailanalyticsengine.repository.OrderRepository;
import com.rishikesan.retailanalyticsengine.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public DataLoader(CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public void load(List<CleanOrderRecord> cleanRecords) {
        for (CleanOrderRecord record : cleanRecords) {
            Customer customer = findOrCreateCustomer(record);
            Product product = findOrCreateProduct(record);

            Order order = new Order();
            order.setCustomer(customer);
            order.setOrderDate(record.getOrderDate());
            order.setStatus(record.getStatus());
            order = orderRepository.save(order);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(record.getQuantity());
            item.setUnitPriceAtSale(record.getUnitPrice());
            orderItemRepository.save(item);
        }
    }

    private Customer findOrCreateCustomer(CleanOrderRecord record) {
        return customerRepository.findByEmail(record.getCustomerEmail())
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setName(record.getCustomerName());
                    c.setEmail(record.getCustomerEmail());
                    c.setSignupDate(record.getOrderDate()); // approximation for this project
                    c.setRegion("Unknown");
                    return customerRepository.save(c);
                });
    }

    private Product findOrCreateProduct(CleanOrderRecord record) {
        return productRepository.findByName(record.getProductName())
                .orElseGet(() -> {
                    Product p = new Product();
                    p.setName(record.getProductName());
                    p.setCategory(record.getCategory());
                    p.setUnitPrice(record.getUnitPrice());
                    return productRepository.save(p);
                });
    }
}