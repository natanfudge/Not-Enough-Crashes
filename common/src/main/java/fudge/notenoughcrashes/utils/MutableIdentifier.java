package fudge.notenoughcrashes.utils;

import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.apache.commons.lang3.StringUtils;

/**
 * Useful for reliably changing the splash logo only when appropriate
 */
public class MutableIdentifier extends Identifier {
    String namespace;
    String path;
    protected MutableIdentifier(String[] id) {
        super(NotEnoughCrashes.MOD_ID,"ignored");
        this.namespace = StringUtils.isEmpty(id[0]) ? "minecraft" : id[0];
        this.path = id[1];
    }

    public MutableIdentifier(String id) {
        this(split(id, ':'));
    }

    public MutableIdentifier(String namespace, String path) {
        super(namespace, path);
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setIdentifier(Identifier newId){
        setNamespace(newId.getNamespace());
        setPath(newId.getPath());
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Identifier)) {
            return false;
        } else {
            Identifier identifier = (Identifier)o;
            return this.namespace.equals(identifier.getNamespace()) && this.path.equals(identifier.getPath());
        }
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    public int compareTo(Identifier identifier) {
        int i = this.path.compareTo(identifier.getPath());
        if (i == 0) {
            i = this.namespace.compareTo(identifier.getNamespace());
        }

        return i;
    }

}
