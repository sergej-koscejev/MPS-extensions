<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:bca3993a-2dc4-4449-a654-c9f2fa86dc9c(de.itemis.mps.editor.collapsible.structure)">
  <persistence version="9" />
  <languages>
    <use id="982eb8df-2c96-4bd7-9963-11712ea622e5" name="jetbrains.mps.lang.resources" version="2" />
    <devkit ref="78434eb8-b0e5-444b-850d-e7c4ad2da9ab(jetbrains.mps.devkit.aspect.structure)" />
  </languages>
  <imports>
    <import index="tpc2" ref="r:00000000-0000-4000-0000-011c8959029e(jetbrains.mps.lang.editor.structure)" />
    <import index="tpee" ref="r:00000000-0000-4000-0000-011c895902ca(jetbrains.mps.baseLanguage.structure)" />
    <import index="tpck" ref="r:00000000-0000-4000-0000-011c89590288(jetbrains.mps.lang.core.structure)" implicit="true" />
  </imports>
  <registry>
    <language id="c72da2b9-7cce-4447-8389-f407dc1158b7" name="jetbrains.mps.lang.structure">
      <concept id="1169125787135" name="jetbrains.mps.lang.structure.structure.AbstractConceptDeclaration" flags="ig" index="PkWjJ">
        <property id="6714410169261853888" name="conceptId" index="EcuMT" />
        <property id="4628067390765907488" name="conceptShortDescription" index="R4oN_" />
        <property id="5092175715804935370" name="conceptAlias" index="34LRSv" />
        <child id="1071489727083" name="linkDeclaration" index="1TKVEi" />
        <child id="1071489727084" name="propertyDeclaration" index="1TKVEl" />
      </concept>
      <concept id="1071489090640" name="jetbrains.mps.lang.structure.structure.ConceptDeclaration" flags="ig" index="1TIwiD">
        <reference id="1071489389519" name="extends" index="1TJDcQ" />
      </concept>
      <concept id="1071489288299" name="jetbrains.mps.lang.structure.structure.PropertyDeclaration" flags="ig" index="1TJgyi">
        <property id="241647608299431129" name="propertyId" index="IQ2nx" />
        <reference id="1082985295845" name="dataType" index="AX2Wp" />
      </concept>
      <concept id="1071489288298" name="jetbrains.mps.lang.structure.structure.LinkDeclaration" flags="ig" index="1TJgyj">
        <property id="1071599776563" name="role" index="20kJfa" />
        <property id="1071599893252" name="sourceCardinality" index="20lbJX" />
        <property id="1071599937831" name="metaClass" index="20lmBu" />
        <property id="241647608299431140" name="linkId" index="IQ2ns" />
        <reference id="1071599976176" name="target" index="20lvS9" />
      </concept>
    </language>
    <language id="ceab5195-25ea-4f22-9b92-103b95ca8c0c" name="jetbrains.mps.lang.core">
      <concept id="1169194658468" name="jetbrains.mps.lang.core.structure.INamedConcept" flags="ng" index="TrEIO">
        <property id="1169194664001" name="name" index="TrG5h" />
      </concept>
    </language>
  </registry>
  <node concept="1TIwiD" id="48DYfEsZko6">
    <property role="TrG5h" value="CellModel_Collapsible" />
    <property role="34LRSv" value="collapsible" />
    <property role="EcuMT" value="4767615435807737350" />
    <ref role="1TJDcQ" to="tpc2:fBEYTCT" resolve="EditorCellModel" />
    <node concept="1TJgyi" id="48DYfEthueu" role="1TKVEl">
      <property role="TrG5h" value="showCollapsedAlways" />
      <property role="IQ2nx" value="4767615435812496286" />
      <ref role="AX2Wp" to="tpck:fKAQMTB" resolve="boolean" />
    </node>
    <node concept="1TJgyi" id="48DYfEtlkSO" role="1TKVEl">
      <property role="TrG5h" value="collapsedByDefault" />
      <property role="IQ2nx" value="4767615435813506612" />
      <ref role="AX2Wp" to="tpck:fKAQMTB" resolve="boolean" />
    </node>
    <node concept="1TJgyi" id="48DYfEtzmNM" role="1TKVEl">
      <property role="TrG5h" value="showBracketLine" />
      <property role="IQ2nx" value="4767615435817184498" />
      <ref role="AX2Wp" to="tpck:fKAQMTB" resolve="boolean" />
    </node>
    <node concept="1TJgyj" id="48DYfEt2oMe" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="expandedCell" />
      <property role="20lbJX" value="1" />
      <property role="IQ2ns" value="4767615435808541838" />
      <ref role="20lvS9" to="tpc2:fBEYTCT" resolve="EditorCellModel" />
    </node>
    <node concept="1TJgyj" id="48DYfEtbX_p" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="collapsedCell" />
      <property role="20lbJX" value="0..1" />
      <property role="IQ2ns" value="4767615435811051865" />
      <ref role="20lvS9" to="tpc2:fBEYTCT" resolve="EditorCellModel" />
    </node>
    <node concept="1TJgyj" id="1$t5g3Q$dBC" role="1TKVEi">
      <property role="IQ2ns" value="1809625719153678824" />
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="glyphWidth" />
      <ref role="20lvS9" node="1$t5g3Q$5tQ" resolve="ConceptFunction_GetGlyphWidth" />
    </node>
    <node concept="1TJgyj" id="69rYimo66sD" role="1TKVEi">
      <property role="IQ2ns" value="7087532378458449705" />
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="paintGlyph" />
      <ref role="20lvS9" node="69rYimo5LH8" resolve="ConceptFunction_PaintGlyph" />
    </node>
  </node>
  <node concept="1TIwiD" id="1$t5g3Q$5tQ">
    <property role="EcuMT" value="1809625719153645430" />
    <property role="TrG5h" value="ConceptFunction_GetGlyphWidth" />
    <property role="34LRSv" value="glyph width" />
    <ref role="1TJDcQ" to="tpee:gyVMwX8" resolve="ConceptFunction" />
  </node>
  <node concept="1TIwiD" id="1$t5g3Q$iVD">
    <property role="EcuMT" value="1809625719153700585" />
    <property role="TrG5h" value="ConceptFunctionParameter_Height" />
    <property role="34LRSv" value="height" />
    <property role="R4oN_" value="function parameter" />
    <ref role="1TJDcQ" to="tpee:g76ryKb" resolve="ConceptFunctionParameter" />
  </node>
  <node concept="1TIwiD" id="69rYimo5LH8">
    <property role="EcuMT" value="7087532378458364744" />
    <property role="TrG5h" value="ConceptFunction_PaintGlyph" />
    <property role="34LRSv" value="paint glyph" />
    <ref role="1TJDcQ" to="tpee:gyVMwX8" resolve="ConceptFunction" />
  </node>
  <node concept="1TIwiD" id="69rYimo5Rzo">
    <property role="EcuMT" value="7087532378458388696" />
    <property role="TrG5h" value="ConceptFunctionParameter_Graphics2D" />
    <property role="34LRSv" value="graphics" />
    <ref role="1TJDcQ" to="tpee:g76ryKb" resolve="ConceptFunctionParameter" />
  </node>
  <node concept="1TIwiD" id="69rYimo65Uo">
    <property role="EcuMT" value="7087532378458447512" />
    <property role="TrG5h" value="ConceptFunctionParameter_Highlighted" />
    <property role="34LRSv" value="highlighted" />
    <ref role="1TJDcQ" to="tpee:g76ryKb" resolve="ConceptFunctionParameter" />
  </node>
  <node concept="1TIwiD" id="69rYimpipxQ">
    <property role="EcuMT" value="7087532378478450806" />
    <property role="TrG5h" value="ConceptFunctionParameter_Bounds" />
    <property role="34LRSv" value="bounds" />
    <ref role="1TJDcQ" to="tpee:g76ryKb" resolve="ConceptFunctionParameter" />
  </node>
</model>

