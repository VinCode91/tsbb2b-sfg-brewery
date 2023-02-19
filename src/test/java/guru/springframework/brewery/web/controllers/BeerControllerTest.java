package guru.springframework.brewery.web.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import guru.springframework.brewery.services.BeerService;
import guru.springframework.brewery.web.model.BeerDto;
import guru.springframework.brewery.web.model.BeerPagedList;
import guru.springframework.brewery.web.model.BeerStyleEnum;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
class BeerControllerTest {
    @MockBean
    BeerService beerService;
    /** Plus besoin d'initialiser mockMvc avec {@link WebMvcTest} */
    @Autowired
    MockMvc mockMvc;

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
    }

    @AfterEach
    void tearDown() {
       // reset(beerService);
    }

    @Test
    void testGetBeerById() throws Exception {
        // given
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        given(beerService.findBeerById(any())).willReturn(validBeer);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/{id}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(validBeer.getId().toString())))
                .andExpect(jsonPath("$.quantityOnHand", is(4)))
                .andExpect(jsonPath("$.beerStyle", is(BeerStyleEnum.PALE_ALE.name())))
                .andExpect(jsonPath("$.createdDate",
                        is(dateTimeFormatter.format(validBeer.getCreatedDate()))))
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Nested
    @DisplayName(("List ops - "))
    class TestListOperations {

        @Captor
        ArgumentCaptor<String> beerNameCaptor;
        @Captor
        ArgumentCaptor<BeerStyleEnum> beerStyleEnumCaptor;
        @Captor
        ArgumentCaptor<PageRequest> pageRequestCaptor;

        BeerPagedList beerPagedList;

        @BeforeEach
        void setUp() {
            List<BeerDto> beers = new ArrayList<>();
            beers.add(validBeer);
            beers.add(BeerDto.builder().id(UUID.randomUUID())
                            .version(1)
                            .beerName("Beer4").upc(123123123123122L).beerStyle(BeerStyleEnum.PALE_ALE)
                            .price(new BigDecimal("12.99"))
                            .createdDate(OffsetDateTime.now())
                            .upc(123456789012L)
                            .lastModifiedDate(OffsetDateTime.now())
                            .build());

            beerPagedList = new BeerPagedList(beers, PageRequest.of(1, 1), 2L);
            given(beerService.listBeers(beerNameCaptor.capture(),
                    beerStyleEnumCaptor.capture(), pageRequestCaptor.capture())).willReturn(beerPagedList);
        }

        @Test
        @DisplayName("Test list beers - no params")
        void testListBeer() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(validBeer.getId().toString())))
            ;
        }

        @Test
        @DisplayName("Test list beers - code coverage for default page params")
        void testListBeerWithParams() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer")
                            .accept(MediaType.APPLICATION_JSON)
                            .param("pageNumber", "-1")
                            .param("pageSize", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(validBeer.getId().toString())))
                    .andReturn();

            System.out.println("Response :" + result.getResponse().getContentAsString());
        }


    }

    /**
     * Le converter doit être utile pour les versions antérieures {@link OffsetDateTime}
     * Non nécessaire dans l'environnement actuel (Java 11 Spring boot 2.7.8)
     * @return
     */
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.registerModule(new JavaTimeModule());
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}


