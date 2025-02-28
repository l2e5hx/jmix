/*
 * Copyright 2021 Haulmont.
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

package test_support.repository;

import io.jmix.core.FetchPlan;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import io.jmix.core.repository.QueryHints;
import io.jmix.data.PersistenceHints;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import test_support.entity.repository.Customer;
import test_support.entity.repository.SalesOrder;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeaturedOrderRepository extends JmixDataRepository<SalesOrder, UUID> {

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Iterable<SalesOrder> findAll();

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Optional<SalesOrder> findById(UUID uuid, FetchPlan fetchPlan);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Iterable<SalesOrder> findAll(FetchPlan fetchPlan);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Iterable<SalesOrder> findAll(Iterable<UUID> uuids, @Nullable FetchPlan fetchPlan);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Iterable<SalesOrder> findAll(Sort sort, @Nullable FetchPlan fetchPlan);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Page<SalesOrder> findAll(Pageable pageable, @Nullable FetchPlan fetchPlan);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Iterable<SalesOrder> findAll(Sort sort);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Page<SalesOrder> findAll(Pageable pageable);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Optional<SalesOrder> findById(UUID uuid);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    boolean existsById(UUID uuid);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Iterable<SalesOrder> findAllById(Iterable<UUID> uuids);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    long count();

    @Override
    void delete(SalesOrder entity);

    @Query("select o from repository$SalesOrder o")
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    List<SalesOrder> loadByQueryWithSoftDeletionFalse();

    @Query("select o from repository$SalesOrder o")
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "true")})
    List<SalesOrder> loadByQueryWithSoftDeletionTrue();

    @Query("select o from repository$SalesOrder o")
    List<SalesOrder> loadAllByQuery();

    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    Page<SalesOrder> findSalesByCustomer(Customer customer, Pageable pageable);

    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    long countCustomersByIdNotNull();

    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    boolean existsByNumber(String number);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    void deleteAllById(Iterable<? extends UUID> uuids);

    @Override
    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    void deleteAll();

    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    void removeByNumber(String number);

    @Query("select o from repository$SalesOrder o")
    @QueryHints({@QueryHint(name = PersistenceHints.CACHEABLE, value = "true")})
    void loadAllWithCache();
}
