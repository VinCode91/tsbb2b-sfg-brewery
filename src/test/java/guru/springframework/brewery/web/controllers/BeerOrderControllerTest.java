package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {
    @MockBean
    BeerOrderService beerOrderService;
    @Autowired
    MockMvc mockMvc;

    BeerOrderDto firstOrder;
    BeerOrderPagedList orderPagedList;
    BeerDto validBeer;

    @BeforeEach
    void setUp() {
        validBeer = BeerDto.builder().id(UUID.randomUUID())
                .version(1)
                .beerName("Beer1")
                .beerStyle(BeerStyleEnum.PALE_ALE)
                .price(new BigDecimal("12.99"))
                .quantityOnHand(4)
                .createdDate(OffsetDateTime.now())
                .upc(123456789012L)
                .lastModifiedDate(OffsetDateTime.now())
                .build();
        List<BeerOrderDto> beers = new ArrayList<>();
        firstOrder = BeerOrderDto.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .orderStatus(OrderStatusEnum.PICKED_UP)
                .createdDate(OffsetDateTime.now().minusHours(2))
                .lastModifiedDate(OffsetDateTime.now().minusMinutes(15))
                .customerRef("Ref 1")
                .beerOrderLines(Arrays.asList(BeerOrderLineDto.builder()
                        .beerId(UUID.randomUUID())
                        .orderQuantity(5)
                        .build(), BeerOrderLineDto.builder()
                        .beerId(UUID.randomUUID())
                        .orderQuantity(2)
                        .build()))
                .build();
        beers.add(firstOrder);

        BeerOrderDto otherOrder = BeerOrderDto.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .orderStatus(OrderStatusEnum.NEW)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .customerRef("Ref 3")
                .beerOrderLines(Arrays.asList(BeerOrderLineDto.builder()
                        .beerId(validBeer.getId())
                        .orderQuantity(6)
                        .build(), BeerOrderLineDto.builder()
                        .beerId(UUID.randomUUID())
                        .orderQuantity(2)
                        .build()))
                .build();
        beers.add(otherOrder);

        orderPagedList = new BeerOrderPagedList(beers, PageRequest.of(1, 1), 2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void listOrders() throws Exception {
        // given
        BDDMockito.given(beerOrderService.listOrders(any(), any())).willReturn(orderPagedList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/{customerId}/orders", UUID.randomUUID())
                .param("pageNumber", String.valueOf(-1))
                .param("pageSize", String.valueOf(0)) // coverage for code handling bad values
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[1].orderStatus", is(OrderStatusEnum.NEW.name())))
                .andExpect(jsonPath("$.content[1].beerOrderLines[0].beerId", is(validBeer.getId().toString())))
                .andExpect(jsonPath("$..beerOrderLines[?(@.orderQuantity > 5)]..beerId", hasItem(validBeer.getId().toString())))
        ;
    }

    @Test
    void getOrder() throws Exception {
        // given
        BDDMockito.given(beerOrderService.getOrderById(any(), any())).willReturn(firstOrder);

        // when - then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/{customerId}/orders/{orderId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(firstOrder.getCustomerId().toString())))
                .andExpect(jsonPath("$.id", is(firstOrder.getId().toString())));
    }
}
