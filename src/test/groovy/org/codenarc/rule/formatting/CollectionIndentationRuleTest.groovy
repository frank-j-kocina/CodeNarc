/*
 * Copyright 2021 the original author or authors.
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
package org.codenarc.rule.formatting

import org.junit.Test
import org.codenarc.rule.AbstractRuleTestCase

/**
 * Tests for CollectionIndentationRule
 *
 * @author Frank J Kocina
 */
class CollectionIndentationRuleTest extends AbstractRuleTestCase<CollectionIndentationRule> {

    @Test
    void test_RuleProperties() {
        assert rule.priority == 2
        assert rule.name == 'CollectionIndentation'
    }

    @Test
    void test_VariousValidFormats_NoViolations() {
        final String SOURCE = '''
            |List l1 = []
            |List l2 = ['chicken']
            |List l3 = ['chicken',
            |           'monkey']
            |List l4 = [
            |        'chicken',
            |        'monkey'
            |]
            |List l5 = [
            |        'chicken',
            |        'monkey']
            |List l6 = ['chicken', 'monkey']
            |List l7 = ['chicken', 'monkey',
            |           'snake', 'walrus',
            |           'unicorn']
            |List l8 = [
            |        'chicken', 'monkey',
            |        'snake',
            |        'walrus', 'unicorn']
            |// acceptable now, but would like to make a violation
            |List l9 = [
            |    'chicken'
            |]
            |List l10 = [
            |    'chicken',
            |    'monkey'
            |]
        '''.stripMargin()

        assertNoViolations(SOURCE)
    }

    @Test
    void test_VariedLevelsOfIndentation_Violations() {
        final String SOURCE = '''
            |List l1 = ['chicken',
            |        'monkey',
            |         'snake']
            |List l2 = [
            |        'chicken',
            |         'monkey'
            |]
            |List l3 = [
            |        'chicken',
            |       'monkey']
        '''.stripMargin()

        assertViolations(
                SOURCE,
                [line: 3, source: "'monkey'", message: 'The collection entry `monkey` is at the incorrect indentation level: Expected column 12 but was 9'],
                [line: 4, source: "'snake'", message: 'The collection entry `snake` is at the incorrect indentation level: Expected column 12 but was 10'],
                [line: 7, source: "'monkey'", message: 'The collection entry `monkey` is at the incorrect indentation level: Expected column 9 but was 10'],
                [line: 11, source: "'monkey'", message: 'The collection entry `monkey` is at the incorrect indentation level: Expected column 9 but was 8'])
    }

    @Test
    void test_AllTypesOfCollections_Violations() {
        final String SOURCE = '''
            |Collection c = [
            |        'chicken',
            |         'monkey']
            |List l = [
            |        'chicken',
            |         'monkey']
            |Set s = [
            |        'chicken',
            |         'monkey']
        '''.stripMargin()

        assertViolations(
                SOURCE,
                [line: 4, source: "'monkey'", message: 'The collection entry `monkey` is at the incorrect indentation level: Expected column 9 but was 10'],
                [line: 7, source: "'monkey'", message: 'The collection entry `monkey` is at the incorrect indentation level: Expected column 9 but was 10'],
                [line: 10, source: "'monkey'", message: 'The collection entry `monkey` is at the incorrect indentation level: Expected column 9 but was 10'])
    }

    @Override
    protected CollectionIndentationRule createRule() {
        new CollectionIndentationRule()
    }
}
