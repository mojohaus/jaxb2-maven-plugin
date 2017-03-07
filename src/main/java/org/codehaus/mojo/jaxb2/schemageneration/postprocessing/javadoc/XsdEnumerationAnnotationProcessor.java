package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.NodeProcessor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.ClassLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.FieldLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.MethodLocation;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.SortedMap;


/**
 * <p>Node processor that injects XSD documentation annotations consisting of JavaDoc harvested Java source code
 * into SimpleTypes, Elements and Attributes typically produced by SchemaGen when generate XSDs for Java Enumerations.
 * The documentation is injected as follows:</p>
 * <ol>
 * <li><strong>SimpleType</strong>: Class-level JavaDoc from the corresponding type is injected as an
 * annotation directly inside the SimpleType.</li>
 * <li><strong>Element</strong>: Field-level JavaDoc (or getter Method-level JavaDoc, in case the Field does
 * not contain a JavaDoc annotation) from the corresponding member is injected as an
 * annotation directly inside the element.</li>
 * <li><strong>Attribute</strong>: Field-level JavaDoc (or getter Method-level JavaDoc, in case the Field does
 * not contain a JavaDoc annotation) from the corresponding member is injected as an
 * annotation directly inside the element.</li>
 * </ol>
 * <p>Thus, the following 'vanilla'-generated XSD:</p>
 * <pre>
 *     <code>
 *           &lt;xs:simpleType name="foodPreference"&gt;
 *               &lt;xs:restriction base="xs:string"&gt;
 *                   &lt;xs:enumeration value="NONE"/&gt;
 *                   &lt;xs:enumeration value="VEGAN"/&gt;
 *                   &lt;xs:enumeration value="LACTO_VEGETARIAN"/&gt;
 *               &lt;/xs:restriction&gt;
 *           &lt;/xs:simpleType&gt;
 *     </code>
 * </pre>
 * <p>... will be converted in a manner similar to the one below:</p>
 * <pre>
 *     <code>
 *         &lt;xs:simpleType name="foodPreference"&gt;
 *             &lt;xs:annotation&gt;
 *                 &lt;xs:documentation&gt;&lt;![CDATA[Simple enumeration example defining some Food preferences.]]&gt;&lt;/xs:documentation&gt;
 *             &lt;/xs:annotation&gt;
 *             &lt;xs:restriction base="xs:string"&gt;
 *                 &lt;xs:enumeration value="LACTO_VEGETARIAN"&gt;
 *                     &lt;xs:annotation&gt;
 *                         &lt;xs:documentation&gt;&lt;![CDATA[Vegetarian who will not eat meats, but drinks milk.]]&gt;&lt;/xs:documentation&gt;
 *                     &lt;/xs:annotation&gt;
 *                 &lt;/xs:enumeration&gt;
 *                 &lt;xs:enumeration value="NONE"&gt;
 *                     &lt;xs:annotation&gt;
 *                         &lt;xs:documentation&gt;&lt;![CDATA[No special food preferences; eats everything.]]&gt;&lt;/xs:documentation&gt;
 *                     &lt;/xs:annotation&gt;
 *                 &lt;/xs:enumeration&gt;
 *                 &lt;xs:enumeration value="VEGAN"&gt;
 *                     &lt;xs:annotation&gt;
 *                         &lt;xs:documentation&gt;&lt;![CDATA[Vegan who will neither eat meats nor drink milk.]]&gt;&lt;/xs:documentation&gt;
 *                     &lt;/xs:annotation&gt;
 *                 &lt;/xs:enumeration&gt;
 *             &lt;/xs:restriction&gt;
 *         &lt;/xs:simpleType&gt;
 *     </code>
 * </pre>
 * <p>... given that the Java class <code>FoodPreference</code> has JavaDoc on its class and fields
 * corresponding to the injected XSD annotation/documentation elements.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XsdEnumerationAnnotationProcessor implements NodeProcessor {

    // Internal state
    private SortedMap<ClassLocation, JavaDocData> classJavaDocs;
    private SortedMap<FieldLocation, JavaDocData> fieldJavaDocs;
    private SortedMap<MethodLocation, JavaDocData> methodJavaDocs;
    private JavaDocRenderer renderer;

    /**
     * Creates an XsdEnumerationAnnotationProcessor that uses the supplied/generated SearchableDocumentation to read all
     * JavaDoc structures and the supplied JavaDocRenderer to render JavaDocs into XSD documentation annotations.
     *
     * @param docs     A non-null SearchableDocumentation, produced from the source code of the JAXB compilation unit.
     * @param renderer A non-null JavaDocRenderer, used to render the JavaDocData within the SearchableDocumentation.
     */
    public XsdEnumerationAnnotationProcessor(final SearchableDocumentation docs, final JavaDocRenderer renderer) {

        // Check sanity
        Validate.notNull(docs, "docs");
        Validate.notNull(renderer, "renderer");

        // Assign internal state
        this.classJavaDocs = docs.getAll(ClassLocation.class);
        this.fieldJavaDocs = docs.getAll(FieldLocation.class);
        this.methodJavaDocs = docs.getAll(MethodLocation.class);
        this.renderer = renderer;
    }

    /**
     * <p>Only accept simpleTypes which are restrictions to either <code>xs:string</code> or <code>xs:integer</code>.
     * The former is generated by JAXB when the Java Enum uses String values, and the latter is used
     * for ordinal values.</p>
     *
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Node aNode) {

        // Only deal with Element nodes.
        if (aNode.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }

        // We should accept:
        //
        // 1) A simpleType Element
        // 2) An enumeration Element
        final Element theElement = (Element) aNode;

        final String localName = theElement.getLocalName();
        if (localName != null) {

            final String trimmed = localName.trim();
            return trimmed.equalsIgnoreCase("enumeration")
                    || trimmed.equalsIgnoreCase("simpleType");
        }

        /*
        <xs:simpleType name="foodPreference">
            <!-- ClassLocation JavaDocData insertion point -->

            <xs:restriction base="xs:string">

                <!-- FieldLocation or MethodLocation JavaDocData insertion point (within child) -->
                <xs:enumeration value="NONE"/>

                <!-- FieldLocation or MethodLocation JavaDocData insertion point (within child) -->
                <xs:enumeration value="LACTO_VEGETARIAN"/>

                <!-- FieldLocation or MethodLocation JavaDocData insertion point (within child) -->
                <xs:enumeration value="VEGAN"/>

            </xs:restriction>
        </xs:simpleType>
         */

        /*
  <xs:simpleType name="foodPreference">
    <xs:restriction base="xs:string">
      <xs:enumeration value="NONE"/>
      <xs:enumeration value="LACTO_VEGETARIAN"/>
      <xs:enumeration value="VEGAN"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="americanCoin">
    <xs:restriction base="xs:int">
      <xs:enumeration value="25"/>
      <xs:enumeration value="1"/>
      <xs:enumeration value="5"/>
      <xs:enumeration value="10"/>
    </xs:restriction>
  </xs:simpleType>
        */

        // All done.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Node aNode) {
        DomHelper.insertXmlDocumentationAnnotationsFor(aNode, classJavaDocs, fieldJavaDocs, methodJavaDocs, renderer);
    }
}
