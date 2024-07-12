package sim.explainer.library.framework.unfolding;

import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.springframework.stereotype.Component;
import sim.explainer.library.enumeration.OWLConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.OWLServiceContext;
import sim.explainer.library.util.OWLOntologyUtil;
import sim.explainer.library.util.ParserUtils;

import java.util.HashSet;
import java.util.Set;

@Component("superRoleUnfolderManchesterSyntax")
public class SuperRoleUnfolderManchesterSyntax implements IRoleUnfolder {

    private OWLServiceContext owlServiceContext;

    public SuperRoleUnfolderManchesterSyntax(OWLServiceContext owlServiceContext) {
        this.owlServiceContext = owlServiceContext;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<String> unfold(OWLObjectProperty owlObjectProperty, Set<String> roles) {
        Set<OWLObjectPropertyExpression> owlObjectPropertyExpressions = owlObjectProperty.getSuperProperties(owlServiceContext.getOwlOntology());

        // When a role has no defined hierarchy.
        if (owlObjectPropertyExpressions.isEmpty()) {
            roles.add(owlObjectProperty.getIRI().getFragment());
        }

        else {
            roles.add(ParserUtils.generateFreshName(owlObjectProperty.getIRI().getFragment()));

            for (OWLObjectPropertyExpression propertyExpression : owlObjectPropertyExpressions) {
                OWLObjectProperty superObjectProperty = propertyExpression.asOWLObjectProperty();

                unfold(superObjectProperty, roles);
            }
        }

        return roles;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Set<String> unfoldRoleHierarchy(String roleName) {
        if (roleName == null) {
            throw new JSimPiException("Unable to unfold role hierarchy as roleName is null.", ErrorCode.SuperRoleUnfolderManchesterSyntax_IllegalArguments);
        }

        Set<String> roles = new HashSet<String>();
        if (roleName.equals(OWLConstant.TOP_ROLE.getOwlSyntax())) {
            return roles;
        }

        OWLObjectProperty owlObjectProperty = OWLOntologyUtil.getOWLObjectProperty(owlServiceContext.getOwlDataFactory(), owlServiceContext.getOwlOntologyManager(), owlServiceContext.getOwlOntology(), roleName);

        return unfold(owlObjectProperty, roles);
    }
}
