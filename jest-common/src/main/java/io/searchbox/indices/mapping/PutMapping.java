package io.searchbox.indices.mapping;

import com.google.gson.Gson;
import io.searchbox.action.GenericResultAbstractAction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author ferhat
 * @author cihat keser
 */
public class PutMapping extends GenericResultAbstractAction {

    private Object source;

    public PutMapping(Builder builder) {
        super(builder);

        this.indexName = builder.index;
        this.typeName = builder.type;
        this.source = builder.source;
        setURI(buildURI());
    }

    @Override
    public Object getData(Gson gson) {
        return source;
    }

    @Override
    protected String buildURI() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.buildURI()).append("/_mapping");
        return sb.toString();
    }

    @Override
    public String getRestMethodName() {
        return "PUT";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(source)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        PutMapping rhs = (PutMapping) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(source, rhs.source)
                .isEquals();
    }

    public static class Builder extends GenericResultAbstractAction.Builder<PutMapping, Builder> {
        private String index;
        private String type;
        private Object source;

        public Builder(String index, String type, Object source) {
            this.index = index;
            this.type = type;
            this.source = source;
        }

        @Override
        public PutMapping build() {
            return new PutMapping(this);
        }
    }

}
