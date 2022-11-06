package org.thingsboard.server.dao.sql.query;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityDataAdapterTest {

    @Test
    public void testConvertValue() {
        assertThat(EntityDataAdapter.convertValue("500")).isEqualTo("500");
        assertThat(EntityDataAdapter.convertValue("500D")).isEqualTo("500D"); //do not convert to Double !!!
        assertThat(EntityDataAdapter.convertValue("0101010521130565")).isEqualTo("0101010521130565");
    }
}
