/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.searchui.component;

import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.ui.component.SingleFilterComponent;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

/**
 * FullTextFilter is a UI component used for filtering entities returned by the {@link io.jmix.ui.model.DataLoader} by
 * joining JPQL query results with the data returned from Elasticsearch. The component renders a {@link
 * io.jmix.ui.component.TextField} for entering full-text search criteria.
 */
@StudioComponent(
        caption = "FullTextFilter",
        category = "Search",
        xmlElement = "fullTextFilter",
        xmlns = "http://jmix.io/schema/search/ui",
        xmlnsAlias = "search",
        canvasBehaviour = CanvasBehaviour.FULL_TEXT_FILTER,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/search/search-in-ui.html#full-text-filter",
        unsupportedProperties = {"captionVisible"}
)
@StudioElement(
        caption = "FullTextFilter",
        xmlElement = "fullTextFilter",
        xmlns = "http://jmix.io/schema/search/ui",
        xmlnsAlias = "search",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/search/search-in-ui.html#full-text-filter",
        unsupportedProperties = {"dataLoader", "captionWidth", "autoApply", "captionPosition", "captionVisible"}
)
public interface FullTextFilter extends SingleFilterComponent<String> {

    String NAME = "fullTextFilter";

    /**
     * Returns a {@link SearchStrategy} that will be used for searching using this component. If no explicit strategy is
     * set to the full-text filter then a default strategy will be used.
     *
     * @return a {@link SearchStrategy} or null if the strategy is not explicitly set
     * @see SearchStrategyManager#getDefaultSearchStrategy()
     */
    @Nullable
    SearchStrategy getSearchStrategy();

    @StudioProperty(name = "strategy", type = PropertyType.STRING, defaultValue = "anyTermAnyField",
            options = {"anyTermAnyField", "allTermsAnyField", "allTermsSingleField", "phrase"})
    void setSearchStrategy(@Nullable SearchStrategy searchStrategy);
}
