/*
 * Copyright (c) 2019, Seran-PC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.swrlapi.example;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;

import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.swrlapi.factory.SWRLAPIFactory;

/**
 *
 * @author Seran-PC
 */
public class Sample {
    private static final String DOCUMENT_IRI = "http://acrab.ics.muni.cz/ontologies/example.owl";
    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, SWRLBuiltInException, SWRLParseException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        // create new empty ontology
        OWLOntology ontology = manager.createOntology(IRI.create(DOCUMENT_IRI));
        //set up prefixes
        DefaultPrefixManager pm = new DefaultPrefixManager();
        pm.setDefaultPrefix(DOCUMENT_IRI + "#");
        pm.setPrefix("var:", "urn:swrl#");

        //class declarations
        OWLClass personClass = factory.getOWLClass(":Person", pm);
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(personClass));

        OWLClass manClass = factory.getOWLClass(":Man", pm);
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(manClass));

        OWLClass englishProgrammerClass = factory.getOWLClass(":EnglishProgrammer", pm);
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(englishProgrammerClass));

        //named individuals declarations
        OWLNamedIndividual english = createIndividual(ontology, pm, manager, ":English");
        OWLNamedIndividual comp = createIndividual(ontology, pm, manager, ":Computer-Programming");
        OWLNamedIndividual john = createIndividual(ontology, pm, manager, ":John");

        //annotated subclass axiom
        OWLAnnotationProperty annotationProperty = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
        OWLAnnotationValue value = factory.getOWLLiteral("States that every man is a person.");
        OWLAnnotation annotation = factory.getOWLAnnotation(annotationProperty, value);
        OWLSubClassOfAxiom subClassOfAxiom = factory.getOWLSubClassOfAxiom(manClass, personClass, Collections.singleton(annotation));
        manager.addAxiom(ontology, subClassOfAxiom);

        //object property declaration
        OWLObjectProperty speaksLanguageProperty = createObjectProperty(ontology, pm, manager, ":speaksLanguage");
        OWLObjectProperty hasKnowledgeOfProperty = createObjectProperty(ontology, pm, manager, ":hasKnowledgeOf");

        //axiom - John is a Person
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(personClass, john));
        //axiom - John speaksLanguage English
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(speaksLanguageProperty, john, english));
        //axiom - John hasKnowledgeOf Computer-Programming
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(hasKnowledgeOfProperty, john, comp));

        //axiom - EnglishProgrammers is equivalent to intersection of classes
        OWLObjectHasValue c1 = factory.getOWLObjectHasValue(speaksLanguageProperty, english);
        OWLObjectHasValue c2 = factory.getOWLObjectHasValue(hasKnowledgeOfProperty, comp);
        OWLObjectIntersectionOf andExpr = factory.getOWLObjectIntersectionOf(personClass, c1, c2);
        manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(englishProgrammerClass, andExpr));



        //SWRL rule - Person(?x),speaksLanguage(?x,English),hasKnowledgeOf(?x,Computer-Programming)->englishProgrammer(?x)
        SWRLVariable varX = factory.getSWRLVariable(pm.getIRI("var:x"));
        Set<SWRLAtom> body = new LinkedHashSet<>();
        body.add(factory.getSWRLClassAtom(personClass, varX));
        body.add(factory.getSWRLObjectPropertyAtom(speaksLanguageProperty, varX, factory.getSWRLIndividualArgument(english)));
        body.add(factory.getSWRLObjectPropertyAtom(hasKnowledgeOfProperty, varX, factory.getSWRLIndividualArgument(comp)));
        Set<? extends SWRLAtom> head = Collections.singleton(factory.getSWRLClassAtom(englishProgrammerClass, varX));
        SWRLRule swrlRule = factory.getSWRLRule(body, head);
        manager.addAxiom(ontology, swrlRule);

        // Person(John)^ speaksLanguage(John,?x)

        SWRLVariable varY = factory.getSWRLVariable(pm.getIRI("John"));
        Set<SWRLAtom> body1 = new LinkedHashSet<>();
        body1.add(factory.getSWRLClassAtom(personClass, varY));
        SWRLVariable varZ = factory.getSWRLVariable(pm.getIRI("var:z"));

        body1.add(factory.getSWRLObjectPropertyAtom(speaksLanguageProperty, varY, factory.getSWRLVariable(pm.getIRI("var:z"))));
        Set<? extends SWRLAtom> head1 = Collections.singleton(factory.getSWRLObjectPropertyAtom(speaksLanguageProperty,varY, varZ));
        SWRLRule swrlRule1 = factory.getSWRLRule(body1, head1);
        manager.addAxiom(ontology, swrlRule1);


        // swrl rules are static rules. They are not queries. So they cannot return results. They will just infer on the owl file and
        // update the owl file with inferred results.

        //sqwrl is the standard query language for owl. It can return results. spaqrql is the standard query language of RDF. So, don`t
        // expect swrl to return results, as those are not queries, just rules enforse and modify the owl file.


        listSWRLRules(ontology, pm);




        //save  to a file
        OWLXMLDocumentFormat ontologyFormat = new OWLXMLDocumentFormat();

        ontologyFormat.copyPrefixesFrom(pm);
        
        String path = "D:\\MSc-BDA\\Semester02\\DataMining\\week08\\SWRLOntologyGenerated\\SWRL.owl";
        path = path.replace("\\", "/");
        manager.saveOntology(ontology, ontologyFormat, IRI.create(new File(path).toURI()));

        //reason
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, new SimpleConfiguration());
        for (OWLNamedIndividual person : reasoner.getInstances(personClass, false).getFlattened()) {
            System.out.println("person : " + renderer.render(person));
        }
        for (OWLNamedIndividual englishProgrammer : reasoner.getInstances(englishProgrammerClass, false).getFlattened()) {
            System.out.println("englishProgrammer : " + renderer.render(englishProgrammer));
        }

        for (OWLNamedIndividual ind : reasoner.getObjectPropertyValues(john, speaksLanguageProperty).getFlattened()) {
            System.out.println("John speaks Language: " + renderer.render(ind));
        }

       /* for (OWLLiteral email : reasoner.getDataPropertyValues(john, hasEmailProperty)) {
            System.out.println("Martin has email: " + email.getLiteral());
        }

        //check whether the SWRL rule is used
        OWLNamedIndividual ivan = factory.getOWLNamedIndividual(":John", pm);
        OWLClass chOMPClass = factory.getOWLClass(":hasKnowledgeOfProperty", pm);
        OWLClassAssertionAxiom axiomToExplain = factory.getOWLClassAssertionAxiom(chOMPClass, ivan);
        System.out.println("Is Ivan child of married parents ? : " + reasoner.isEntailed(axiomToExplain));


*/


        boolean result = reasoner.isEntailed(factory.getOWLObjectPropertyAssertionAxiom(speaksLanguageProperty, john, english));
        System.out.println("Can John Speak Engligh ? : " + result);








    }

    public static void listSWRLRules(OWLOntology ontology, DefaultPrefixManager pm) {
        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
        for (SWRLRule rule : ontology.getAxioms(AxiomType.SWRL_RULE)) {
            System.out.println("Created SWRL Rule is = "+renderer.render(rule));
        }
    }

    private static OWLNamedIndividual createIndividual(OWLOntology ontology, DefaultPrefixManager pm, OWLOntologyManager manager, String name) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLNamedIndividual individual = factory.getOWLNamedIndividual(name, pm);
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(individual));
        return individual;
    }

    private static OWLObjectProperty createObjectProperty(OWLOntology ontology, DefaultPrefixManager pm, OWLOntologyManager manager, String name) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(name, pm);
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(objectProperty));
        return objectProperty;


    }

}
