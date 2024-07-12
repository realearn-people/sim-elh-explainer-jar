package sim.explainer.library.framework;

import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.util.ParserUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class KRSSServiceContext {

    private static final Logger logger = LoggerFactory.getLogger(KRSSServiceContext.class);

    private File krssFile;

    private Map<String, String> fullConceptDefinitionMap = new HashMap<String, String>();;
    private Map<String, String> primitiveConceptDefinitionMap = new HashMap<String, String>();
    private Map<String, String> fullRoleDefinitionMap = new HashMap<String, String>();
    private Map<String, String> primitiveRoleDefinitionMap = new HashMap<String, String>();

    private static final Pattern PATTERN_FULL_CONCEPT_DEFINITION =
            Pattern.compile("^\\(define\\-concept ([a-zA-Z]+[0-9_']*|[0-9_']+) (.+)\\)");
    private static final Pattern PATTERN_PRIMITIVE_CONCEPT_DEFINITION =
            Pattern.compile("^\\(define\\-primitive-concept ([a-zA-Z]+[0-9_']*|[0-9_']+) (.+)\\)");
    private static final Pattern PATTERN_FULL_ROLE_DEFINITION =
            Pattern.compile("^\\(define\\-role ([a-zA-Z]+[0-9_']*|[0-9_']+) (.+)\\)");
    private static final Pattern PATTERN_PRIMITIVE_ROLE_DEFINITION =
            Pattern.compile("^\\(define\\-primitive-role ([a-zA-Z]+[0-9_']*|[0-9_']+) (.+)\\)");

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void resetAllMaps() {
        this.fullConceptDefinitionMap.clear();
        this.primitiveConceptDefinitionMap.clear();
        this.fullRoleDefinitionMap.clear();
        this.primitiveRoleDefinitionMap.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean instantiateAFullConceptDefinition(String definition) {
        if (definition == null) {
            throw new JSimPiException("Unable to instantiate a full concept definition as definition is null.", ErrorCode.KRSSServiceContext_IllegalArguments);
        }

        String compactDefinition = ParserUtils.compactConceptDescriptionString(definition);

        Matcher matcher = PATTERN_FULL_CONCEPT_DEFINITION.matcher(compactDefinition);
        if (matcher.matches()) {
            String name = matcher.group(1);
            String description = matcher.group(2);

            if (fullConceptDefinitionMap.containsKey(name)) {
                throw new JSimPiException("Unable to instantiate a full concept definition as it found " +
                        "duplicated defined full concept name[" + name + "].", ErrorCode.KRSSServiceContext_NotDefinatorialTBoxException);
            }

            fullConceptDefinitionMap.put(name, description);

            return true;
        }

        return false;
    }

    protected boolean instantiateAPrimitiveConceptDefinition(String definition) {
        if (definition == null) {
            throw new JSimPiException("Unable to instantiate a primitive concept definition as definition is null.", ErrorCode.KRSSServiceContext_IllegalArguments);
        }

        String compactDefinition = ParserUtils.compactConceptDescriptionString(definition);

        Matcher matcher = PATTERN_PRIMITIVE_CONCEPT_DEFINITION.matcher(compactDefinition);
        if (matcher.matches()) {
            String name = matcher.group(1);
            String description = matcher.group(2);

            if (primitiveConceptDefinitionMap.containsKey(name)) {
                throw new JSimPiException("Unable to instantiate a primitive concept definition as it found " +
                        "duplicated defined primitive concept name[" + name + "].", ErrorCode.KRSSServiceContext_NotDefinatorialTBoxException);
            }

            String freshName = ParserUtils.generateFreshName(name);

            StringBuilder builder = new StringBuilder("(and");
            builder.append(StringUtils.SPACE);
            builder.append(freshName);
            builder.append(StringUtils.SPACE);
            builder.append(description);
            builder.append(")");

            primitiveConceptDefinitionMap.put(name, builder.toString());

            return true;
        }

        return false;
    }

    protected boolean instantiateAFullRoleDefinition(String definition) {
        if (definition == null) {
            throw new JSimPiException("Unable to instantiate a full role definition as definition is null.", ErrorCode.KRSSServiceContext_IllegalArguments);
        }

        String compactDefinition = ParserUtils.compactConceptDescriptionString(definition);

        Matcher matcher = PATTERN_FULL_ROLE_DEFINITION.matcher(compactDefinition);
        if (matcher.matches()) {
            String name = matcher.group(1);
            String description = matcher.group(2);

            if (fullRoleDefinitionMap.containsKey(name)) {
                throw new JSimPiException("Unable to instantiate a full role definition as it found " +
                        "duplicated defined full role name[" + name + "].", ErrorCode.KRSSServiceContext_NotDefinatorialTBoxException);
            }

            fullRoleDefinitionMap.put(name, description);

            return true;
        }

        return false;
    }

    protected boolean instantiateAPrimitiveRoleDefinition(String definition) {
        if (definition == null) {
            throw new JSimPiException("Unable to instantiate a primitive role definition as definition is null.", ErrorCode.KRSSServiceContext_IllegalArguments);
        }

        String compactDefinition = ParserUtils.compactConceptDescriptionString(definition);

        Matcher matcher = PATTERN_PRIMITIVE_ROLE_DEFINITION.matcher(compactDefinition);
        if (matcher.matches()) {
            String name = matcher.group(1);
            String description = matcher.group(2);

            if (primitiveRoleDefinitionMap.containsKey(name)) {
                throw new JSimPiException("Unable to instantiate a primitive role definition as it found " +
                        "duplicated defined primitive role name[" + name + "].", ErrorCode.KRSSServiceContext_NotDefinatorialTBoxException);
            }

            String freshName = ParserUtils.generateFreshName(name);

            StringBuilder builder = new StringBuilder("(and");
            builder.append(StringUtils.SPACE);
            builder.append(freshName);
            builder.append(StringUtils.SPACE);
            builder.append(description);
            builder.append(")");

            primitiveRoleDefinitionMap.put(name, builder.toString());

            return true;
        }

        return false;
    }

    protected boolean readKRSSFile(String krssFilePath) {
        if (krssFilePath == null) {
            throw new JSimPiException("Unable to read krss file as krssFilePath is null.", ErrorCode.KRSSServiceContext_IllegalArguments);
        }

        BufferedReader bufferedReader = null;

        try {
            resetAllMaps();

            bufferedReader = new BufferedReader(new FileReader(krssFilePath));
            String readLine;
            StringBuilder builder = new StringBuilder();
            int firstParenthesis = -1;
            while ((readLine = bufferedReader.readLine()) != null) {

                builder.append(readLine);

                if (firstParenthesis == -1) {
                    firstParenthesis = StringUtils.indexOf(readLine, ParserUtils.OPEN_PARENTHESIS_STR);
                }

                int lastMatchedParenthesis = ParserUtils.getLastMatchedCloseParenthesis(builder.toString());

                // Validate if the syntax is well-formed or not.
                if (firstParenthesis > -1 && lastMatchedParenthesis == -1) {
                    continue;
                }

                else {
                    firstParenthesis = -1;
                }

                // Otherwise, invoke business logic and reset the builder.
                String wellFormedStr = builder.toString();
                builder.setLength(0);

                boolean hasFullConceptDefinition = instantiateAFullConceptDefinition(wellFormedStr);
                boolean hasPrimitiveConceptDefinition = instantiateAPrimitiveConceptDefinition(wellFormedStr);

                if (hasFullConceptDefinition && hasPrimitiveConceptDefinition) {
                    throw new JSimPiException("Unable to read krss file as it found " +
                            "duplicated well-formed str[" + wellFormedStr + "].", ErrorCode.KRSSServiceContext_NotDefinatorialTBoxException);
                }

                boolean hasFullRoleDefinition = instantiateAFullRoleDefinition(wellFormedStr);
                boolean hasPrimitiveRoleDefinition = instantiateAPrimitiveRoleDefinition(wellFormedStr);

                if (hasFullRoleDefinition && hasPrimitiveRoleDefinition) {
                    throw new JSimPiException("Unable to read krss file as it found " +
                            "duplicated well-formed str[" + wellFormedStr
                            + "].", ErrorCode.KRSSServiceContext_NotDefinatorialTBoxException);
                }
            }

            return true;
        }

        catch (FileNotFoundException e) {
            resetAllMaps();
            throw new JSimPiException("Unable to read krss file from path[" + krssFilePath + "] due to file not found exception.", e, ErrorCode.KRSSServiceContext_FileNotFoundException);
        }

        catch (IOException e) {
            resetAllMaps();
            throw new JSimPiException("Unable to read krss file from path[" + krssFilePath + "] due to file not found exception.", e, ErrorCode.KRSSServiceContext_IOException);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void init(String krssFilePath) {
        if (krssFilePath == null) {
            throw new JSimPiException("Unable to init krss service context as krssFilePath is null.", ErrorCode.KRSSServiceContext_IllegalArguments);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Loading KRSS file from path[" + krssFilePath + "].");
        }

        this.krssFile = new File(krssFilePath);
        readKRSSFile(krssFilePath);

        if (logger.isInfoEnabled()) {
            logger.info("KRSS file from path[" + krssFilePath + "] has been loaded.");
        }
    }

    public void resetFullConceptDefinitionMap() {
        this.fullConceptDefinitionMap.clear();
    }

    public void resetPrimitiveConceptDefinitionMap() {
        this.primitiveConceptDefinitionMap.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public File getKrssFile() {
        return krssFile;
    }

    public Map<String, String> getFullConceptDefinitionMap() {
        return fullConceptDefinitionMap;
    }

    public Map<String, String> getPrimitiveConceptDefinitionMap() {
        return primitiveConceptDefinitionMap;
    }

    public Map<String, String> getFullRoleDefinitionMap() {
        return fullRoleDefinitionMap;
    }

    public Map<String, String> getPrimitiveRoleDefinitionMap() {
        return primitiveRoleDefinitionMap;
    }

}