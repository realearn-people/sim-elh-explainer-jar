package sim.explainer.library.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.unfolding.SuperRoleUnfolderManchesterSyntax;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PreferenceProfile {

    private static final Logger logger = LoggerFactory.getLogger(PreferenceProfile.class);

    private Map<String, BigDecimal> primitiveConceptImportance = new HashMap<String, BigDecimal>();
    private Map<String, BigDecimal> roleImportance = new HashMap<String, BigDecimal>();
    private Map<String, Map<String, BigDecimal>> primitiveConceptsSimilarity = new HashMap<String, Map<String, BigDecimal>>();
    private Map<String, Map<String, BigDecimal>> primitiveRolesSimilarity = new HashMap<String, Map<String, BigDecimal>>();
    private Map<String, BigDecimal> roleDiscountFactor = new HashMap<String, BigDecimal>();

    private BigDecimal defaultRoleDiscountFactor = BigDecimal.valueOf(0.4);

    @Autowired
    private OWLServiceContext OWLServiceContext;
    @Autowired
    private SuperRoleUnfolderManchesterSyntax superRoleUnfolderManchesterSyntax;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addPrimitiveConceptImportance(String key, BigDecimal val) {
        if (key == null || val == null) {
            throw new JSimPiException("Unable to add primitive concept importance as key[" + key + "] and val[" + val + "] are null.", ErrorCode.PreferenceProfile_IllegalArguments);
        }

//        // Validate input
//        boolean isFreshName = OWLOntologyUtil.isValidFreshConceptName(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key);
//
//        // 1. Throw an exception if key is not used in the ontology
//        OWLClass owlClass = OWLOntologyUtil.getOWLClass(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key);
//        boolean classExist = OWLOntologyUtil.containClassName(OWLServiceContext.getOwlOntology(), owlClass);
//        if (!isFreshName && !classExist) {
//            throw new JSimPiException("Unable to add primitive concept importance as key[" + key + "] is not used in the ontology.", ErrorCode.PreferenceProfile_NotUsedConceptNameException);
//        }
//
//        // 2. Throw an exception if key is not a primitive
//        String definition = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key);
//        if (!isFreshName && definition != null) {
//            throw new JSimPiException("Unable to add primitive concept importance as key[" + key + "] is not a primitive concept.", ErrorCode.PreferenceProfile_NotPrimitiveException);
//        }
//
//        // 3. Throw an exception if val is less than 0.
//        if (val.compareTo(BigDecimal.ZERO) < 0) {
//            throw new JSimPiException("Unable to add primitive concept importance as key[" + key + "] is mapped to a negative number val[" + val + "].", ErrorCode.PreferenceProfile_NegativeNumberException);
//        }

        // Invoke business logic
        this.primitiveConceptImportance.put(key, val);
    }

    public void addRoleImportance(String key, BigDecimal val) {
        if (key == null || val == null) {
            throw new JSimPiException("Unable to add role importance as key[" + key + "] and val[" + val + "] are null.", ErrorCode.PreferenceProfile_IllegalArguments);
        }

//        // Validate input
//        boolean isFreshName = OWLOntologyUtil.isValidFreshRoleName(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key);
//
//        // 1. Throw an exception if key is not used in the ontology.
//        OWLObjectProperty owlObjectProperty = OWLOntologyUtil.getOWLObjectProperty(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key);
//        boolean propertyExist = OWLOntologyUtil.containObjectPropertyName(OWLServiceContext.getOwlOntology(), owlObjectProperty);
//        if(!isFreshName && !propertyExist) {
//            throw new JSimPiException("Unable to add role importance as key[" + key + "] is not used in the ontology.", ErrorCode.PreferenceProfile_NotUsedRoletNameException);
//        }
//
//        // 2. Throw an exception if val is less than 0.
//        if (val.compareTo(BigDecimal.ZERO) < 0) {
//            throw new JSimPiException("Unable to add role importance as key[" + key + "] is mapped to a negative number val[" + val + "].", ErrorCode.PreferenceProfile_NegativeNumberException);
//        }

        // Invoke business logic
        this.roleImportance.put(key, val);
    }

    public void addPrimitveConceptsSimilarity(String key1, String key2, BigDecimal val) {
        if (key1 == null || key2 == null || val == null) {
            throw new JSimPiException("Unable to add primitive concepts similarity as key1[" + key1 + "], key2[" + key2 + "], and val[" + val + "] are null.", ErrorCode.PreferenceProfile_IllegalArguments);
        }

//        // Validate input
//        boolean isFreshName1 = OWLOntologyUtil.isValidFreshConceptName(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key1);
//        boolean isFreshName2 = OWLOntologyUtil.isValidFreshConceptName(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key2);
//
//        // 1. Throw an exception if key1 and key2 are not used in the ontology
//        OWLClass owlClass1 = OWLOntologyUtil.getOWLClass(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key1);
//        boolean class1Exist = OWLOntologyUtil.containClassName(OWLServiceContext.getOwlOntology(), owlClass1);
//
//        OWLClass owlClass2 = OWLOntologyUtil.getOWLClass(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key2);
//        boolean class2Exist = OWLOntologyUtil.containClassName(OWLServiceContext.getOwlOntology(), owlClass2);
//
//        if ( (!isFreshName1 && !class1Exist) || (!isFreshName2 && !class2Exist) ) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "isFreshName1: " + isFreshName1);
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "class1Exist: " + class1Exist);
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "isFreshName2: " + isFreshName2);
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "class2Exist: " + class2Exist);
//            }
//            throw new JSimPiException("Unable to add primitive concepts similarity as key1[" + key1 + "] and key2[" + key2 + "] are not used in the ontology.", ErrorCode.PreferenceProfile_NotUsedConceptNameException);
//        }
//
//        // 2. Throw an exception if key1 and key2 are not primitive
//        String definition1 = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key1);
//        String definition2 = OWLConceptDefinitionUtil.generateFullConceptDefinitionManchesterSyntax(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key2);
//
//        if ( (!isFreshName1 && definition1 != null) || (!isFreshName2 && definition2 != null) ) {
//            throw new JSimPiException("Unable to add primitive concepts similarity as definition1[" + definition1 + "] and definition2[" + definition2 + "] are not primitive.", ErrorCode.PreferenceProfile_NotPrimitiveException);
//        }
//
//        // 3. Throw an exception if val is less than 0 or is greater than 1
//        if (val.compareTo(BigDecimal.ZERO) < 0 || val.compareTo(BigDecimal.ZERO) > 1) {
//            throw new JSimPiException("Unable to add primitive concept similarity as key[" + key1 + "] and key2[" + key2 + "] are" +
//                    "mapped to val[" + val.toPlainString() + "] which is less than 0 or is greater than 1.", ErrorCode.PreferenceProfile_NotUnitIntervalException);
//        }

        // Invoke business logic
        // teeradaj@20160328: Primitive concepts similarity must preserve the symmetric property.
        Map<String, BigDecimal> subKeys1 = this.primitiveConceptsSimilarity.get(key1);
        if (subKeys1 == null) {
            subKeys1 = new HashMap<String, BigDecimal>();
        }
        subKeys1.put(key2, val);
        this.primitiveConceptsSimilarity.put(key1, subKeys1);

        Map<String, BigDecimal> subKeys2 = this.primitiveConceptsSimilarity.get(key2);
        if (subKeys2 == null) {
            subKeys2 = new HashMap<String, BigDecimal>();
        }
        subKeys2.put(key1, val);
        this.primitiveConceptsSimilarity.put(key2, subKeys2);
    }

    public void addPrimitiveRolesSimilarity(String key1, String key2, BigDecimal val) {
        if (key1 == null || key2 == null || val == null) {
            throw new JSimPiException("Unable to add primitive roles similarity as key1[" + key1 + "], key2[" + key2 + "], and val[" + val + "] are null.", ErrorCode.PreferenceProfile_IllegalArguments);
        }

//        // Validate input
//        boolean isFreshName1 = OWLOntologyUtil.isValidFreshRoleName(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key1);
//        boolean isFreshName2 = OWLOntologyUtil.isValidFreshRoleName(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key2);
//
//        // 1. Throw an exception if key1 and key2 are not used in the ontology.
//        OWLObjectProperty owlObjectProperty1 = OWLOntologyUtil.getOWLObjectProperty(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key1);
//        boolean property1Exist = OWLOntologyUtil.containObjectPropertyName(OWLServiceContext.getOwlOntology(), owlObjectProperty1);
//
//        OWLObjectProperty owlObjectProperty2 = OWLOntologyUtil.getOWLObjectProperty(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key2);
//        boolean property2Exist = OWLOntologyUtil.containObjectPropertyName(OWLServiceContext.getOwlOntology(), owlObjectProperty2);
//
//        if ( (!isFreshName1 && !property1Exist) || (!isFreshName2 && !property2Exist) ) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "isFreshName1: " + isFreshName1);
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "property1Exist: " + property1Exist);
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "isFreshName2: " + isFreshName2);
//                logger.debug("key1[" + key1 + "] key2[" + key2 + "] " + "property2Exist: " + property2Exist);
//            }
//
//            throw new JSimPiException("Unable to add primitive roles similarity as key1[" + key1 + "] and key2[" + key2 + "] are not used in the ontology.", ErrorCode.PreferenceProfile_NotUsedRoletNameException);
//        }
//
//        // 2. Throw an exception if key1 and key2 are not primitive
//        Set<String> superRoles1 = superRoleUnfolderManchesterSyntax.unfoldRoleHierarchy(key1);
//        Set<String> superRoles2 = superRoleUnfolderManchesterSyntax.unfoldRoleHierarchy(key2);
//
//        if (superRoles1.size() != 1 || superRoles2.size() != 1) {
//            throw new JSimPiException("Unable to add primitive roles similarity as key1[" + key1 + "] and key2[" + key2 + "] are not primitive.", ErrorCode.PreferenceProfile_NotPrimitiveRoleException);
//        }
//
//        // 3. Throw an exception if val is lexx than 0 and greater than 1.
//        if (val.compareTo(BigDecimal.ZERO) < 0 || val.compareTo(BigDecimal.ONE) > 1) {
//            throw new JSimPiException("Unable to add primitive roles similarity as key1[" + key1 + "] and key2[" + key2 + "] are mapped to val[" +
//                    val + "] which is less than 0 or greater than 1.", ErrorCode.PreferenceProfile_NotUnitIntervalException);
//        }

        // Invoke business logic
        // teeradaj@20180328: Primitive roles similarity must preserve the symmetric property.
        Map<String, BigDecimal> subKeys1 = this.primitiveRolesSimilarity.get(key1);
        if (subKeys1 == null) {
            subKeys1 = new HashMap<String, BigDecimal>();
        }
        subKeys1.put(key2, val);
        this.primitiveRolesSimilarity.put(key1, subKeys1);

        Map<String, BigDecimal> subKeys2 = this.primitiveRolesSimilarity.get(key2);
        if (subKeys2 == null) {
            subKeys2 = new HashMap<String, BigDecimal>();
        }
        subKeys2.put(key1, val);
        this.primitiveRolesSimilarity.put(key2, subKeys2);
    }

    public void addRoleDiscountFactor(String key, BigDecimal val) {
        if (key == null || val == null) {
            throw new JSimPiException("Unable to add role discount factor as key[" + key + "] and val[" + val + "] are null.", ErrorCode.PreferenceProfile_IllegalArguments);
        }

//        // Validate input
//        boolean isFreshName = OWLOntologyUtil.isValidFreshRoleName(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key);
//
//        // 1. Throw an exception if key is not used in the ontology
//        OWLObjectProperty owlObjectProperty = OWLOntologyUtil.getOWLObjectProperty(OWLServiceContext.getOwlDataFactory(), OWLServiceContext.getOwlOntologyManager(), OWLServiceContext.getOwlOntology(), key);
//        boolean propertyExist = OWLOntologyUtil.containObjectPropertyName(OWLServiceContext.getOwlOntology(), owlObjectProperty);
//
//        if (!isFreshName && !propertyExist) {
//            throw new JSimPiException("Unable to add role discount factor as key[" + key + "] is not used in the ontology.", ErrorCode.PreferenceProfile_NotUsedRoletNameException);
//        }
//
//        // 2. Throw an exception if val is less than 0 or is greater than 1.
//        if (val.compareTo(BigDecimal.ZERO) < 0 || val.compareTo(BigDecimal.ONE) > 1) {
//            throw new JSimPiException("Unable to add role discount factor as key[" + key + "] is mapped to val[" + val + "] " +
//                    "which is less than 0 or is greater than 1.", ErrorCode.PreferenceProfile_NotUnitIntervalException);
//        }

        this.roleDiscountFactor.put(key, val);
    }

    public void setDefaultRoleDiscountFactor(BigDecimal roleDiscountFactor) {
        defaultRoleDiscountFactor = roleDiscountFactor;
    }

    public void reset() {
        this.primitiveConceptImportance.clear();
        this.roleImportance.clear();
        this.primitiveConceptsSimilarity.clear();
        this.primitiveRolesSimilarity.clear();
        this.roleDiscountFactor.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Map<String, BigDecimal> getPrimitiveConceptImportance() {
        return primitiveConceptImportance;
    }

    public Map<String, BigDecimal> getRoleImportance() {
        return roleImportance;
    }

    public Map<String, Map<String, BigDecimal>> getPrimitiveConceptsSimilarity() {
        return primitiveConceptsSimilarity;
    }

    public Map<String, Map<String, BigDecimal>> getPrimitiveRolesSimilarity() {
        return primitiveRolesSimilarity;
    }

    public Map<String, BigDecimal> getRoleDiscountFactor() {
        return roleDiscountFactor;
    }

    public BigDecimal getDefaultRoleDiscountFactor() { return defaultRoleDiscountFactor; }
}