Changes in Turtle files for better display, and some explanations

1. Changed sh:node property to sh:class where sh:nodeKind is sh:IRI and no Children shape is needed.
   - InstantiatedVirtualResourceShape  "maintained by" and "tenant owned by"
   - PhysicalResourceShape "owned by", "maintained by" and "manufactured by"
   - DataResourceShape "produced by"  and "exposed through"
2. Combined sh:or with sh:in
    - DataResourceShape   "license" - displays only the string option
    - SoftwareResourceShape "license" - displays only the string option
    - AddressShape       "countryCode" ;
           sh:or with three sh:in changed to only one sh:in  
3. ServiceOfferingLabelShape had invalid property "assigned to" with sh:or, parentheses were missing 
4. LegalRegistrationNumberShape has an sh:or before the properties, it is left out from JSON
