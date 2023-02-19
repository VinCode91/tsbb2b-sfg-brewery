/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package guru.springframework.brewery.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class BeerOrderPagedList extends PageImpl<BeerOrderDto> {
    /** IMPERATIF pour le test d'intégration
     * sinon RestTemplate ne peut construire la réponse à la requête
     * Cause : l'objet Pageable est abstrait (interface) et RestTemplate ne gère que les types simples
     * ou les objets construits avec des types simples
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BeerOrderPagedList(@JsonProperty List<BeerOrderDto> content,
                              @JsonProperty int number,
                              @JsonProperty int size,
                              @JsonProperty Long totalElements,
                              @JsonProperty JsonNode pageable,
                              @JsonProperty boolean last,
                              @JsonProperty int totalPages,
                              @JsonProperty JsonNode sort,
                              @JsonProperty boolean first,
                              @JsonProperty int numberOfElements) {
        super(content, PageRequest.of(number, size), totalPages);
    }

    public BeerOrderPagedList(List<BeerOrderDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BeerOrderPagedList(List<BeerOrderDto> content) {
        super(content);
    }
}
