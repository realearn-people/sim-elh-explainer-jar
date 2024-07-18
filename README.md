# sim-elh-explainer-jar

---

## Usage

To use the `SimExplainer` library, follow these steps:

1. **Instantiate the SimExplainer**

    You can instantiate the `SimExplainer` using different sets of input files.

    **Using a Directory Containing Both Ontology and Preference Profile Files**
    ```java
    SimExplainer explainer = new SimExplainer("path/to/ontologyAndProfileDirectory");
    ```

    **Using Separate Directories for Ontology and Preference Profile Files**
    ```java
    SimExplainer explainer = new SimExplainer("path/to/ontologyDirectory", "path/to/preferenceProfileDirectory");
    ```

    **Using Individual File Paths**
    ```java
    SimExplainer explainer = new SimExplainer(
        "path/to/ontologyFile",
        "path/to/primitiveConceptImportanceFile",
        "path/to/roleImportanceFile",
        "path/to/primitiveConceptsSimilarityFile",
        "path/to/primitiveRolesSimilarityFile",
        "path/to/roleDiscountFactorFile"
    );
    ```
    **Every path, except the Ontology path, can be left as null.*

    **Input Files**

    When initializing the `SimExplainer` with a directory, the following files will be automatically read if present:

   - **Ontology file**: The last file found with the extension `.owl` or `.krss` will be used.
   - **Primitive Concept Importance file**: The last file that starts with "primitive-concept-importance" will be used.
   - **Role Importance file**: The last file that starts with "role-importance" will be used.
   - **Primitive Concept Similarity file**: The last file that starts with "primitive-concepts-similarity" will be used.
   - **Primitive Role Similarity file**: The last file that starts with "primitive-roles-similarity" will be used.
   - **Role Discount Factor file**: The last file that starts with "role-discount-factor" will be used.

    **Preference Profile Files**

    You can load these files manually using the following methods:
    ```java
    void ReadInputPrimitiveConceptImportances(String pathToFile) throws IOException
    void ReadInputRoleImportances(String pathToFile) throws IOException
    void ReadInputPrimitiveConceptsSimilarities(String pathToFile) throws IOException 
    void ReadInputPrimitiveRolesSimilarities(String pathToFile) throws IOException
    void ReadInputRoleDiscountFactors(String pathToFile) throws IOException
    ```

    You can reset the preference profile with this method:
    ```java
    void resetPreferenceProfile()
    ```

2. **Retrieve Concept Names from the Loaded Ontology**

    This retrieves all concept names from the loaded ontology.
    ```java
    List<String> conceptNames = explainer.retrieveConceptName();
    conceptNames.forEach(System.out::println);
    ```

3. **Measure Similarity Between Concept Names in the Loaded Ontology**

    This measures the similarity between two concepts.
    ```java
    BigDecimal similarity = explainer.simMeasurementResult(TypeConstant.SOME_OPTION, "Concept1", "Concept2");
    System.out.println("Similarity: " + similarity);
    ```

    The `TypeConstant` enum consists of the following constants:
    
   - `DYNAMIC_SIM`: dynamic programming Sim
   - `DYNAMIC_SIMPI`: dynamic programming SimPi
   - `TOPDOWN_SIM`: top down Sim
   - `TOPDOWN_SIMPI`: top down SimPi

4. **Retrieve Tree Hierarchy**

    This retrieves the tree hierarchy for a given concept.
    ```java
    String hierarchy = explainer.treeHierachy("Concept1");
    System.out.println("Tree Hierarchy: " + hierarchy);
    ```

5. **Retrieve Explanation**

    This retrieves the explanation for the similarity measurement between two concepts.
    ```java
    SimExplainer.Explanation explanation = explainer.getExplanation("Concept1", "Concept2");
    System.out.println("Forward Explanation: " + explanation.forward);
    System.out.println("Backward Explanation: " + explanation.backward);
    ```

## Publications

- Teeradaj Racharak, "On Approximation of Concept Similarity Measure in Description Logic ELH with Pre-trained Word Embedding," In IEEE Access, vol. 9, pp. 61429-61443, 2021. DOI: 10.1109/ACCESS.2021.3073730
- Teeradaj Racharak, Boontawee Suntisrivaraporn, and Satoshi Tojo, "Personalizing a Concept Similarity Measure in the Description Logic ELH with Preference Profile," In Computing and Informatics vol. 37, no. 3, pp. 581-613, 2018. DOI: 10.4149/cai_2018_3_581