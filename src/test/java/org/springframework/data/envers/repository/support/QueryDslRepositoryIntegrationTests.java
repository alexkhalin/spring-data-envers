/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.envers.repository.support;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.envers.Config;
import org.springframework.data.envers.sample.Country;
import org.springframework.data.envers.sample.CountryQuerydslRepository;
import org.springframework.data.envers.sample.QCountry;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration tests for repositories with QueryDsl support. They make sure that methods provided by both
 * {@link EnversRevisionRepository} and {@link org.springframework.data.querydsl.QueryDslPredicateExecutor} are working.
 *
 * @author Dmytro Iaroslavskyi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class QueryDslRepositoryIntegrationTests {

	@Autowired CountryQuerydslRepository countryRepository;

	@Before
	public void setUp() {
		countryRepository.deleteAll();
	}

	@Test
	public void testWithQueryDsl() {

		Country de = new Country();
		de.code = "de";
		de.name = "Deutschland";

		countryRepository.save(de);

		Optional<Country> found = countryRepository.findOne(QCountry.country.name.eq("Deutschland"));

		assertThat(found.isPresent(), is(true));
		assertThat(found.get().id, is(de.id));

	}

	@Test
	public void testWithRevisions() {

		Country de = new Country();
		de.code = "de";
		de.name = "Deutschland";

		countryRepository.save(de);

		de.name = "Germany";

		countryRepository.save(de);

		Revisions<Integer, Country> revisions = countryRepository.findRevisions(de.id);

		assertThat(revisions, is(Matchers.<Revision<Integer, Country>>iterableWithSize(2)));

		Iterator<Revision<Integer, Country>> iterator = revisions.iterator();
		Revision<Integer, Country> first = iterator.next();
		Revision<Integer, Country> second = iterator.next();

		assertThat(countryRepository.findRevision(de.id, first.getRevisionNumber().get()).get().getEntity().name, is("Deutschland"));
		assertThat(countryRepository.findRevision(de.id, second.getRevisionNumber().get()).get().getEntity().name, is("Germany"));

	}

}
