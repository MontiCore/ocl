// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util;

import com.google.common.base.Preconditions;
import de.monticore.antlr4.MCConcreteParser;
import de.monticore.ast.ASTNode;
import de.monticore.symboltable.IGlobalScope;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParserUtil {

  // Util class - do not instantiate
  protected ParserUtil() {
  }

  public static Optional<? extends ASTNode> parse(String filename, MCConcreteParser p) {
    Preconditions.checkArgument(filename != null);
    Optional<? extends ASTNode> cd;
    try {
      cd = p.parse(filename);
      return cd;
    }
    catch (IOException e) {
      Log.error("Could not access " + filename
        + ", there was an I/O exception: " + e.getMessage());
    }
    return Optional.empty();
  }

  public static Collection<? extends ASTNode> parse(Path path, String fileEnding,
    MCConcreteParser p) {
    Preconditions.checkArgument(path != null);
    try (Stream<Path> walk = Files.walk(path)) {
      return walk.filter(Files::isRegularFile)
        .filter(f -> f.getFileName().toString().endsWith(fileEnding))
        .map(f -> parse(f.toString(), p))
        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }
    catch (IOException e) {
      Log.error("Could not access " + path.toString()
        + ", there was an I/O exception: " + e.getMessage());
    }
    return new HashSet<>();
  }

  public static Collection<? extends ASTNode> parseModels(IGlobalScope scope, String fileEnding,
    MCConcreteParser parser) {
    Preconditions.checkArgument(scope != null);
    return scope.getModelPath().getFullPathOfEntries().stream()
      .flatMap(p -> parse(p, fileEnding, parser).stream())
      .collect(Collectors.toSet());
  }
}
