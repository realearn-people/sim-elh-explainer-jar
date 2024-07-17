package sim.explainer.library.exception;

public enum ErrorCode {

    // Application
    Application_IllegalArguments("Application_IllegalArguments"),
    Application_InvalidFileType("Application_InvalidFileType"),
    Application_InvalidPath("Application_InvalidPath"),

    // Controller
    OwlSimilarityController_IllegalArguments("OwlSimilarityController_IllegalArguments"),
    OwlSimilarityController_InvalidConceptNames("OwlSimilarityController_InvalidConceptNames"),
    KrssSimilarityController_IllegalArguments("KrssSimilarityController_IllegalArguments"),
    KrssSimilarityController_InvalidConceptNames("KrssSimilarityController_InvalidConceptNames"),

    // Framework
    ConceptUnfolderManchesterSyntax_IllegalArguments("ConceptUnfolderManchesterSyntax_IllegalArguments"),
    KRSSServiceContext_IllegalArguments("KRSSServiceContext_IllegalArguments"),
    KRSSServiceContext_FileNotFoundException("KRSSServiceContext_FileNotFoundException"),
    KRSSServiceContext_IOException("KRSSServiceContext_IOException"),
    KRSSServiceContext_NotDefinatorialTBoxException("KRSSServiceContext_NotDefinatorialTBoxException"),
    OWLServiceContext_IllegalArguments("OWLServiceContext_IllegalArguments"),
    PreferenceProfile_IllegalArguments("PreferenceProfile_IllegalArguments"),
    PreferenceProfile_NotUsedConceptNameException("PreferenceProfile_NotUsedConceptNameException"),
    PreferenceProfile_NotUsedRoletNameException("PreferenceProfile_NotUsedRoletNameException"),
    PreferenceProfile_NotPrimitiveException("PreferenceProfile_NotPrimitiveException"),
    PreferenceProfile_NotPrimitiveRoleException("PreferenceProfile_NotPrimitiveRoleException"),
    PreferenceProfile_NotUnitIntervalException("PreferenceProfile_NotUnitIntervalException"),
    PreferenceProfile_NegativeNumberException("PreferenceProfile_NegativeNumberException"),
    ServiceContext_OWLOntologyCreationException("ServiceContext_OWLOntologyCreationException"),
    SuperRoleUnfolderManchesterSyntax_IllegalArguments("SuperRoleUnfolderManchesterSyntax_IllegalArguments"),

    // Framework Descriptiontree
    TreeBuilder_IllegalArguments("TreeBuilder_IllegalArguments"),

    // Framework Reasoner
    DynamicProgrammingSimReasonerImpl_IllegalArguments("DynamicProgrammingSimReasonerImpl_IllegalArguments"),
    DynamicProgrammingSimPiReasonerImpl_IllegalArguments("DynamicProgrammingSimPiReasonerImpl_IllegalArguments"),
    TopDownSimReasonerImpl_IllegalArguments("TopDownSimReasonerImpl_IllegalArguments"),
    TopDownSimPiReasonerImpl_IllegalArguments("TopDownSimPiReasonerImpl_IllegalArguments"),

    // Framework Unfolding
    ConceptDefinitionUnfolderKRSSSyntax_IllegalArguments("ConceptDefinitionUnfolderKRSSSyntax_IllegalArguments"),
    ConceptDefinitionUnfolderKRSSSyntax_InvalidRoleNameException("ConceptDefinitionUnfolderKRSSSyntax_InvalidRoleNameException"),
    SuperRoleUnfolderKRSSSyntax_IllegalArguments("SuperRoleUnfolderKRSSSyntax_IllegalArguments"),
    SuperRoleUnfolderManchesterSyntax_InvalidRoleNameException("SuperRoleUnfolderManchesterSyntax_InvalidRoleNameException"),

    // Service
    OWLSimService_IllegalArguments("OWLSimService_IllegalArguments"),

    // Util
    OWLConceptDefinitionUtil_IllegalArguments("OWLConceptDefinitionUtil_IllegalArguments"),
    OWLConceptDefinitionUtil_NotUniqueDefinition("OWLConceptDefinitionUtil_NotUniqueDefinition"),
    OWLOntologyUtil_IllegalArguments("OWLOntologyUtil_IllegalArguments"),
    OWLOntologyUtil_NotUniqueDefinition("OWLOntologyUtil_NotUniqueDefinition"),
    OWLOntologyUtil_OWLOntologyStorageException("OWLOntologyUtil_OWLOntologyStorageException"),
    ParserUtils_IllegalArguments("ParserUtils_IllegalArguments"),

    // Util Syntaxanalyzer Krss
    KrssConceptSetHandler_IllegalArguments("KrssConceptSetHandler_IllegalArguments"),
    KrssRoleSetHandler_IllegalArguments("KrssRoleSetHandler_IllegalArguments"),
    KrssTopLevelParserHandler_IllegalArguments("KrssTopLevelParserHandler_IllegalArguments"),
    KrssTopLevelParserHandler_InEquivalentParenthesisNumbers("KrssTopLevelParserHandler_InEquivalentParenthesisNumbers"),
    KrssTopLevelParserHandler_InvalidSyntaxException("KrssTopLevelParserHandler_InvalidSyntaxException"),

    // Util Syntaxanalyzer Manchester
    ManchesterConceptSetHandler_IllegalArguments("ManchesterConceptSetHandler_IllegalArguments"),
    ManchesterTopLevelParserHandler_IllegalArguments("ManchesterTopLevelParserHandler_IllegalArguments"),
    ManchesterTopLevelParserHandler_InEquivalentParenthesisNumbers("ManchesterTopLevelParserHandler_InEquivalentParenthesisNumbers"),
    ManchesterTopLevelParserHandler_InvalidSyntaxException("ManchesterTopLevelParserHandler_InvalidSyntaxException");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
