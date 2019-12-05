//package fudge.utils;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.function.Predicate;
//
//import net.minecraft.resource.Resource;
//import net.minecraft.resource.ResourceImpl;
//import net.minecraft.resource.ResourceManager;
//import net.minecraft.resource.ResourcePack;
//import net.minecraft.util.Identifier;
//
///**
// * Empty resource manager for minecraft to consume when crashing on init, to prevent wasting time loading stuff.
// */
//public class EmptyResourceManager implements ResourceManager {
//    public static final ResourceManager INSTANCE = new EmptyResourceManager();
//
//    private EmptyResourceManager() {
//    }
//
//    private Set<String> namespaces = new HashSet<>();
//    private List<Resource> resources = new ArrayList<>();
//    private Collection<Identifier> foundResources = new ArrayList<>();
//
//    private Resource resource = new ResourceImpl("<empty>", new Identifier("<empty>", "<empty>"), Files.newInputStream())
//
//    @Override
//    public Set<String> getAllNamespaces() {
//        return namespaces;
//    }
//
//    @Override
//    public Resource getResource(Identifier id) throws IOException {
//        return null;
//    }
//
//    @Override
//    public boolean containsResource(Identifier id) {
//        return false;
//    }
//
//    @Override
//    public List<Resource> getAllResources(Identifier id) throws IOException {
//        return resources;
//    }
//
//    @Override
//    public Collection<Identifier> findResources(String resourceType, Predicate<String> pathPredicate) {
//        return foundResources;
//    }
//
//    @Override
//    public void addPack(ResourcePack pack) {
//
//    }
//}
