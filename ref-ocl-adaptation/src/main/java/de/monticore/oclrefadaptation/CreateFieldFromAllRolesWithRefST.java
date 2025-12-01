package de.monticore.oclrefadaptation;

import de.monticore.cd4analysis.trafo.CDAssociationCreateFieldsFromAllRoles;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdassociation._ast.ASTCDAssocSide;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdassociation._symboltable.CDRoleSymbol;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconcretization.stereotype.StereotypeUtil;
import de.monticore.cdconformance.inc.CDIncarnationMapping;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.umlstereotype._ast.ASTStereotype;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.Validate;

import java.util.Optional;
import java.util.Set;

/**
 * A transformation that creates fields for all roles in a concrete CD, while making sure to
 * add stereotypes so the created fields are recognized as incarnations of the related fields
 * in the reference CD.<br>
 * <br>
 * The basic transformation is still the same as {@link CDAssociationCreateFieldsFromAllRoles},
 * we only use the information in {@link #createdFields} and {@link #fieldToRoles} to add
 * the stereotypes.
 */
public class CreateFieldFromAllRolesWithRefST extends CDAssociationCreateFieldsFromAllRoles {

  private static final String LOG_NAME = CreateFieldFromAllRolesWithRefST.class.getSimpleName();

  /**
   * Incarnation mapping between concrete and reference CD before applying the field from roles
   * transformation.
   */
  protected final CDIncarnationMapping incarnationMapping;

  /** Name of the mapping (used for the stereotype). */
  protected final String mapping;

  /**
   * If true, the transformation will always add the stereotype to the created fields,
   * even if the name is exactly the same as the reference field name.<br>
   * This is required if the conformance parameter
   * {@link de.monticore.cdconformance.CDConfParameter#NAME_MAPPING} is not set.
   */
  protected final boolean alwaysAddStereotype;

  public CreateFieldFromAllRolesWithRefST(
          CDIncarnationMapping incarnationMapping,
          String mapping,
          boolean alwaysAddStereotype) {
    this.incarnationMapping = incarnationMapping;
    this.mapping = mapping;
    this.alwaysAddStereotype = alwaysAddStereotype;
  }

  @Override
  public void transform(ASTCDCompilationUnit concreteCD) throws RuntimeException {
    // 1. Apply the transformation as usual
    super.transform(concreteCD);

    // 2. Attach stereotypes to the created fields, so they are recognized as incarnations of the
    // original field
    for (FieldSymbol createdField : createdFields.keySet()) {
      CDRoleSymbol role = fieldToRoles.get(createdField);
      Optional<ASTCDAssociation> assoc = findAssociation(concreteCD, role.getAssocSide());
      if (assoc.isPresent()) {
        addStereotypesToField(createdField, role, assoc.get());
      } else {
        Log.error("Could not find association for role " + role.getFullName() +
                " in concrete CD " + CD4CodeMill.prettyPrint(concreteCD, false));
      }
    }
  }

  /**
   * Adds a stereotype to the given concrete field symbol, so it is recognized as an
   * incarnation of the reference field symbol which was created for the related reference role.
   *
   * @param conField the concrete field symbol that was created for the role
   * @param conRole the concrete role symbol
   */
  protected void addStereotypesToField(FieldSymbol conField, CDRoleSymbol conRole, ASTCDAssociation conAssoc) {
    Validate.notNull(conField);
    Validate.notNull(conRole);
    Validate.isTrue(conField.isPresentAstNode(), "Concrete field symbol must have an AST" +
            " node (it was literally just created by this transformation and linked to the AST).");
    ASTCDAttribute conAttribute = (ASTCDAttribute) conField.getAstNode();

    Optional<CDRoleSymbol> refRoleOpt = getReferenceRole(conRole, conAssoc);
    if (refRoleOpt.isPresent()) {
      CDRoleSymbol refRole = refRoleOpt.get();
      // 1. add mapping stereotype if required
      // Normally, we only add the stereotype if the name differs form the ref name
      if (alwaysAddStereotype || !refRole.getName().equals(conRole.getName())) {
        // Reference field name is the name of the reference role
        StereotypeUtil.addStereotype(conAttribute.getModifier(), mapping, refRole.getName());
        Log.info("Added stereotype " + mapping + " with value " + refRole.getName() +
                " to field " + conField.getName(), LOG_NAME);
      }

      // 2. transfer bind stereotype from concrete assoc and assoc side to concrete field
      if (conAssoc.getModifier().isPresentStereotype() && conAssoc.getModifier().getStereotype().contains(StereotypeUtil.BIND_STEREOTYPE)) {
        StereotypeUtil.addStereotype(conAttribute.getModifier(), StereotypeUtil.BIND_STEREOTYPE, conAssoc.getModifier().getStereotype().getValue(StereotypeUtil.BIND_STEREOTYPE));
        Log.info("Added bind stereotype fom association to field " + conField.getName()
                + " from association " + conAssoc.getName(), LOG_NAME);
      }
      if (conRole.getAssocSide().getModifier().isPresentStereotype() && conRole.getAssocSide().getModifier().getStereotype().contains(StereotypeUtil.BIND_STEREOTYPE)) {
        StereotypeUtil.addStereotype(conAttribute.getModifier(), StereotypeUtil.BIND_STEREOTYPE, conRole.getAssocSide().getModifier().getStereotype().getValue(StereotypeUtil.BIND_STEREOTYPE));
        Log.info("Added bind stereotype from association side to field " + conField.getName()
                + " from association " + conAssoc.getName(), LOG_NAME);
      }
    } else {
      Log.error("Could not find reference role for concrete role " + conRole.getFullName());
    }
  }

  /// NOTE: This is not yet supported by out incarnation mapping abstraction. Therefore this workaround
  // TODO use incMapping to get reference element
  protected Optional<CDRoleSymbol> getReferenceRole(CDRoleSymbol conRole, ASTCDAssociation conAssoc) {
    Set<ASTCDAssociation> refAssocs = incarnationMapping.getReferenceElements(conAssoc);
    if (refAssocs.isEmpty()) {
      Log.error("No reference association found for concrete association "
              + CD4CodeMill.prettyPrint(conAssoc, false) +
              " with role " + conRole.getName() + ". This should not happen as we check conformance before executing this trafo.");
    } else if (refAssocs.size() > 1) {
      Log.error("A single association incarnating multiple reference associations is not supported yet!");
      // TODO we could add multiple ref stereotypes but this is not yet parsed correctly in conformance check anyway!
    } else {
      ASTCDAssociation refAssoc = refAssocs.iterator().next();
      if (conRole.getAssocSide().isLeft()) {
        ASTCDAssocSide leftSide = refAssoc.getLeft();
        if (leftSide.isPresentCDRole()) {
          return Optional.of(leftSide.getCDRole().getSymbol());
        } else {
          Log.error("Left side of reference association " + refAssoc.getName() +
                  " does not have a role. The CD should already be transformed to have all implicit role names explicitly added.");
        }
      } else {
        ASTCDAssocSide rightSide = refAssoc.getRight();
        if (rightSide.isPresentCDRole()) {
          return Optional.of(rightSide.getCDRole().getSymbol());
        } else {
          Log.error("Right side of reference association " + refAssoc.getName() +
                  " does not have a role. The CD should already be transformed to have all implicit role names explicitly added.");
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Finds the association of which the given AssocSide is a part of (left or right).
   *
   * @param cd the CD to search in
   * @param assocSide the association side to search for
   * @return an Optional containing the found association, or empty if not found
   */
  protected Optional<ASTCDAssociation> findAssociation(ASTCDCompilationUnit cd, ASTCDAssocSide assocSide) {
    for (ASTCDAssociation assoc : cd.getCDDefinition().getCDAssociationsList()) {
      if (assoc.getLeft() == assocSide || assoc.getRight() == assocSide) {
        return Optional.of(assoc);
      }
    }
    return Optional.empty();
  }
}
