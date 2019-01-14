package com.microsoft.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MetadataFileItemTest {

    @Test
    public void testEquals() {
        MetadataFileItem object1 = new MetadataFileItem("123");
        MetadataFileItem object2 = new MetadataFileItem("1234");
        MetadataFileItem object3 = new MetadataFileItem("123");

        assertThat("Should be equal to self", object1.equals(object1), is(true));
        assertThat("Should not be equal to null", object1.equals(null), is(false));
        assertThat("Should not be equal to object of another type", object1.equals(123), is(false));
        assertThat("Should not be equal to object with another uid", object1.equals(object2), is(false));
        assertThat("Should be equal to object with same uid", object1.equals(object3), is(true));
    }

    @Test
    public void testHashCode() {
        String uid = "123";
        MetadataFileItem object = new MetadataFileItem(uid);

        assertThat("Wrong result", object.hashCode(), is(uid.hashCode()));
    }
}
