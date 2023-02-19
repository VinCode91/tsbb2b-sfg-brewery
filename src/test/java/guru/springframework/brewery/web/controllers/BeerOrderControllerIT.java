package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.domain.Customer;
import guru.springframework.brewery.repositories.CustomerRepository;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BeerOrderControllerIT {
    TestRestTemplate restTemplate;
    CustomerRepository customerRepository;

    @Autowired
    public BeerOrderControllerIT(TestRestTemplate restTemplate, CustomerRepository customerRepository) {
        this.restTemplate = restTemplate;
        this.customerRepository = customerRepository;
    }

    @Test
    void testListOrders() {
        Customer testCustomer = customerRepository.findAll().stream().findFirst().orElse(null);
        assertThat(testCustomer).isNotNull();

        BeerOrderPagedList pagedList = restTemplate.getForObject("/api/v1/customers/{customerId}/orders",
                BeerOrderPagedList.class, testCustomer.getId());

        assertThat(pagedList.getContent()).hasSize(1);
        assertThat(pagedList.getContent().get(0).getCustomerRef()).isEqualTo("testOrder1");
    }
}
