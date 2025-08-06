# sim-elh-explainer-jar

---

## Project Overview
SimExplainer is a robust Java library designed to facilitate the computation of similarity between concepts in ontologies. Leveraging Description Logic ELH, this library supports both OWL and KRSS file formats and offers a rich set of features for loading, processing, and explaining concept similarities based on user-defined preference profiles. The project aims to provide researchers and developers with a powerful tool for semantic similarity analysis in various domains.

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

   If you didn't set a Role Discount Factor, there is a default value provided (default = 0.4). But you can still set the default value with this method:
    ```java
    void setDefaultRoleDiscountFactor(BigDecimal value)
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
    BigDecimal similarity = explainer.similarity(ImplementationMethod.DYNAMIC_SIM, "Concept1", "Concept2");
    System.out.println("Similarity: " + similarity);
    ```

   The `ImplementationMethod` enum consists of the following constants:

   - `DYNAMIC_SIM`: dynamic programming Sim
   - `DYNAMIC_SIMPI`: dynamic programming SimPi
   - `TOPDOWN_SIM`: top down Sim
   - `TOPDOWN_SIMPI`: top down SimPi

4. **Retrieve Tree Hierarchy**

   This retrieves the tree hierarchy for a given concept.
    ```java
    String hierarchy = explainer.treeHierarchy("Concept1");
    System.out.println("Tree Hierarchy: " + hierarchy);
    ```

5. **Retrieve Explanation**

   This retrieves the explanation for the similarity measurement between two concepts.
    ```java
    SimExplainer.Explanation explanation = explainer.getExplanation("Concept1", "Concept2");
    System.out.println("Similarity Degree: " + explanation.similarity);
    System.out.println("Forward Explanation: " + explanation.forward);
    System.out.println("Backward Explanation: " + explanation.backward);
    ```

6. **Retrieve Explanation as JSON**

   This retrieves the explanation as a JSON object.
   ```java
   JSONObject explanationJson = explainer.getExplanationAsJson("Concept1", "Concept2");
   System.out.println(explanationJson.toString(4));
   ```

   **Save Explanation as JSON**
   This saves the explanation as a JSON file.

   ```java
   JSONObject explanationJson = explainer.getExplanationAsJson("Concept1", "Concept2", "path/to/outputFile.json");
   System.out.println(explanationJson.toString(4));
   ```

7. **Convert Explanation to Natural Language**

   To use the following function, you have to set up the OpenAI API Key and API Response Timeout.
   ```java
   void setApiTimeout(int apiTimeout); // in seconds, default is 45
   void setApiKey(String apiKey); // must set this
   ```

   This converts the explanation for the similarity between two concepts into natural language.
   ```java
   JSONObject naturalLanguageExplanation = explainer.getExplantionAsNaturalLanguage("Concept1", "Concept2");
   System.out.println(naturalLanguageExplanation.toString(4));
   ```

   **Save Explanation as Natural Language**
   This saves the natural language explanation as a JSON file.
   ```java
   JSONObject naturalLanguageExplanation = explainer.getExplantionAsNaturalLanguage("Concept1", "Concept2", "path/to/outputFile.json");
   System.out.println(naturalLanguageExplanation.toString(4));
   ```

---

## Reading Explanation

### Description Tree

A Description Tree provides a structured way to represent concepts and their relationships in a hierarchical format. This can be represented in JSON for machine readability and in ASCII for human readability.

#### Understanding the Description Tree

**JSON Representation**

The JSON format of a Description Tree is designed to be easily parsed by software applications. Here is the general structure:

```json
{
   "conceptName": "RootConcept",
   "primitiveConcepts": ["PrimitiveConcept"],
   "existentials": [
      {
         "conceptName": "ChildConcept1",
         "primitiveConcepts": ["PrimitiveConcept1"],
         "roleName": "roleName1",
         "existentials": [
            // Further child concepts
         ]
      },
      {
         "conceptName": "ChildConcept2",
         "primitiveConcepts": ["PrimitiveConcept2"],
         "roleName": "roleName2",
         "existentials": []
      }
   ]
}
```

**Components:**

- **conceptName**: The name of the concept.
- **primitiveConcepts**: A list of primitive concepts that define the concept in the current subtree.
- **existentials**: A list of child concepts, each with its own structure including conceptName, primitiveConcepts, roleName, and further existentials.

**ASCII Representation**

The ASCII format is a human-readable representation of the Description Tree. It uses indentation and lines to illustrate the hierarchy:

```
└── RootConcept : [PrimitiveConcept]
    ├── roleName1 : [PrimitiveConcept1]
    │   └── ChildConcept1
    └── roleName2 : [PrimitiveConcept2]
        └── ChildConcept2
```

**Components:**

- **RootConcept**: The main concept at the root of the tree.
- **[PrimitiveConcept]**: The primitive concepts that define the root concept.
- **roleName1, roleName2**: The roles that connect the root concept to its child concepts.
- **ChildConcept1, ChildConcept2**: The child concepts connected to the root concept via specific roles.
- **Indentation and Lines**: Indentation and lines are used to show the hierarchical structure and relationships.

### Explanation Tree

An Explanation Tree provides a structured way to represent the comparison between two concepts and their relationships in a hierarchical format. This can be represented in JSON for machine readability and in ASCII for human readability.

#### Understanding the SimRecord

A SimRecord is the core structure used to describe the similarity between two concepts in the Explanation Tree. It includes:

- **deg**: The homomorphism degree between the concepts.
- **pri**: A list of pairs of primitive concepts from each concept that contribute to the similarity degree. The first component in each pair is from the first concept, and the second component is from the second concept.


- **exi**: A list of pairs of existential concepts from each concept that contribute to the similarity degree. The first component in each pair is from the first concept, and the second component is from the second concept.
- **emb**: A map where the key is a pair of existential/primitive concept pairs, and the value is a set of pairs of roles or primitive concepts that have found similarity within the embedding space. The first component in each pair is from the first concept, and the second component is from the second concept. This key-value structure enhances the similarity degree by showing detailed semantic relationships. The value is a set of pairs that describe the similarity between two primitive concepts or roles, along with their embeddings that contribute to the similarity degree.

**Example of SimRecord**

Here is an example SimRecord and how to read it:

**Explanation in one direction of 'ActivePlace' and 'Mangrove'**

```json
{
    "pri": ["(Place, Place)"],
    "deg": 0.8259457964,
    "exi": [
        "(some canSail Kayaking, some canWalk Trekking)",
        "(some canWalk Trekking, some canWalk Trekking)"
    ],
    "emb": {
        "(some canSail Kayaking, some canWalk Trekking)": [{
            "first": "canTravelWithSail",
            "second": "canMoveWithLegs"
        }]
    }
}
```

**Explanation:**

- **deg**: The homomorphism degree is 0.8259457964. This value indicates how similar the two concepts are.
- **pri**: The primitive concept "Place" in "ActivePlace" is compared with "Place" in "Mangrove". Since they are the same, it increases the similarity degree.
- **exi**:
   - The existential concept "(some canSail Kayaking, some canWalk Trekking)" is compared. Since "canSail Kayaking" and "canWalk Trekking" are different, embeddings are examined.
   - The existential concept "(some canWalk Trekking, some canWalk Trekking)" is the same, which further increases the similarity degree.
- **emb**:
   - For the pair "(some canSail Kayaking, some canWalk Trekking)", the roles "canTravelWithSail" for "Kayaking" and "canMoveWithLegs" for "Trekking" are found to be similar in the embedding space. This contributes to the similarity degree.

#### Explanation Tree Formats

The Explanation Tree can be represented in two formats: JSON and ASCII. Both formats utilize the SimRecord structure to convey the similarity information between concepts.

**JSON Representation**

The JSON format is designed for machine readability. The general structure of an Explanation Tree in JSON format is as follows:

```json
{
    "similarity": similarity_score,
    "forward": { /* forward explanation object */ },
    "backward": { /* backward explanation object */ }
}
```

The main similarity score is derived by averaging the similarity degrees from both the forward and backward explanations. The formula is as follows:

\[ \text{similarity\_score} = \frac{\text{forward\_deg} + \text{backward\_deg}}{2} \]

A forward/backward explanation object will have the following structure:

```json
{
    "children": [ /* explanation object (recursive structure) */ ],
    "pri": [ /* set of primitive concept pairs from SimRecord */ ],
    "deg": similarity_degree,  // homomorphism degree from SimRecord
    "exi": [ /* set of existential pairs from SimRecord */ ],
    "comparingConcept2": "Concept2",
    "emb": { /* map of embeddings from SimRecord */ },
    "comparingConcept1": "Concept1"
}
```

**ASCII Representation**

The ASCII format is a human-readable representation of the Explanation Tree. It uses indentation and lines to illustrate the hierarchy.

**Explanation in one direction (A -> B):**

```
└── [ConceptA] : [ConceptB] - SimRecord{deg=similarity_degree, pri=[(PrimitiveConceptX, PrimitiveConceptY)], exi=[(ExistentialX, ExistentialY)], emb={}}
    ├── [ConceptC] : [ConceptD] - SimRecord{deg=similarity_degree, pri=[(PrimitiveConceptC1, PrimitiveConceptD1)], exi=[(ExistentialC1, ExistentialD1)], emb={(ExistentialC1, ExistentialD1): [(RoleC1, RoleD1)]}}
```

- **[ConceptA] : [ConceptB]**: This line indicates the similarity between `ConceptA` and `ConceptB` in one direction (A -> B). The SimRecord describes the details of this similarity.

---

## Principal Investigator
- Teeradaj Racharak (Tohoku University, Japan)
   - Email: racharak@tohoku.ac.jp


---

## Publications

- Teeradaj Racharak, "On Approximation of Concept Similarity Measure in Description Logic ELH with Pre-trained Word Embedding," In IEEE Access, vol. 9, pp. 61429-61443, 2021. DOI: [10.1109/ACCESS.2021.3073730](https://doi.org/10.1109/ACCESS.2021.3073730)
- Teeradaj Racharak, Boontawee Suntisrivaraporn, and Satoshi Tojo, "Personalizing a Concept Similarity Measure in the Description Logic ELH with Preference Profile," In Computing and Informatics vol. 37, no. 3, pp. 581-613, 2018. DOI: [10.4149/cai_2018_3_581](https://doi.org/10.4149/cai_2018_3_581)
