package com.microsoft.model;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Test
    public void setTypeParameters() {
        MetadataFileItem object = new MetadataFileItem("123");
        List<TypeParameter> typeParams = new ArrayList<>();

        object.setTypeParameters(typeParams);

        assertThat("Syntax should not be null", object.getSyntax(), is(notNullValue()));
        assertThat("Wrong typeParameters value", object.getSyntax().getTypeParameters(), is(typeParams));
    }

    @Test
    public void setTypeParametersWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        List<TypeParameter> typeParams = new ArrayList<>();

        object.setTypeParameters(typeParams);

        assertThat("Syntax object should remain the same", object.getSyntax(), is(existingSyntax));
        assertThat("Wrong typeParameters value", object.getSyntax().getTypeParameters(), is(typeParams));
    }

    @Test
    public void setParameters() {
        MetadataFileItem object = new MetadataFileItem("123");
        List<MethodParameter> params = new ArrayList<>();

        object.setParameters(params);

        assertThat("Syntax should not be null", object.getSyntax(), is(notNullValue()));
        assertThat("Wrong parameters value", object.getSyntax().getParameters(), is(params));
    }

    @Test
    public void setParametersWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        List<MethodParameter> params = new ArrayList<>();

        object.setParameters(params);

        assertThat("Syntax object should remain the same", object.getSyntax(), is(existingSyntax));
        assertThat("Wrong parameters value", object.getSyntax().getParameters(), is(params));
    }

    @Test
    public void setReturn() {
        MetadataFileItem object = new MetadataFileItem("123");
        Return returnValue = new Return("type");

        object.setReturn(returnValue);

        assertThat("Syntax should not be null", object.getSyntax(), is(notNullValue()));
        assertThat("Wrong return value", object.getSyntax().getReturnValue(), is(returnValue));
    }

    @Test
    public void setReturnWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        Return returnValue = new Return("type");

        object.setReturn(returnValue);

        assertThat("Syntax object should remain the same", object.getSyntax(), is(existingSyntax));
        assertThat("Wrong return value", object.getSyntax().getReturnValue(), is(returnValue));
    }

    @Test
    public void setContent() {
        MetadataFileItem object = new MetadataFileItem("123");
        String content = "Some content";

        object.setContent(content);

        assertThat("Syntax should not be null", object.getSyntax(), is(notNullValue()));
        assertThat("Wrong content value", object.getSyntax().getContent(), is(content));
    }

    @Test
    public void setContentWhenSyntaxAlreadyPresent() {
        MetadataFileItem object = new MetadataFileItem("123");
        Syntax existingSyntax = new Syntax();
        object.setSyntax(existingSyntax);
        String content = "Some content";

        object.setContent(content);

        assertThat("Syntax object should remain the same", object.getSyntax(), is(existingSyntax));
        assertThat("Wrong content value", object.getSyntax().getContent(), is(content));
    }

    @Test
    public void setInheritance() {
        MetadataFileItem object = new MetadataFileItem("123");

        object.setInheritance(Arrays.asList("Some value"));

        assertThat("Wrong inheritance size", object.getInheritance().size(), is(1));
        assertThat("Wrong inheritance content", object.getInheritance(), hasItem("Some value"));
    }

    @Test
    public void setInheritanceForNull() {
        MetadataFileItem object = new MetadataFileItem("123");

        object.setInheritance(null);

        assertThat("Wrong inheritance", object.getInheritance(), is(nullValue()));
    }

    @Test
    public void getIsExternal() {
        assertThat("Wrong isExternal when null", (new MetadataFileItem("123")).getIsExternal(), is(nullValue()));
        assertThat("Wrong isExternal when true", (new MetadataFileItem("123", "name", true)).getIsExternal(), is(true));
        assertThat("Wrong isExternal when false", (new MetadataFileItem("123", "name", false)).getIsExternal(),
            is(nullValue()));
    }
}
