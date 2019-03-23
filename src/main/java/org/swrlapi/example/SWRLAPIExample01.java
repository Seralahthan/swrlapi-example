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

import java.io.File;


import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;

/**
 *
 * @author Seran-PC
 */
public class SWRLAPIExample01 {
      private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
  //private static final String DOCUMENT_IRI = "http://acrab.ics.muni.cz/ontologies/example.owl";

  public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, SWRLParseException, SWRLBuiltInException {
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    //File with an existing ontology - make sure it's there!
//        File file = new File("C:/Users/Kaneeka Vidanage/Downloads/family.owl");
    String path = "D:\\MSc-BDA\\Semester02\\DataMining\\week08\\SWRLOntologyGenerated\\SWRL.owl";
    path = path.replace("\\", "/");
    File file = new File(path);
    //Load the ontology from the file
    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
    //Check if the ontology contains any axioms
    System.out.println("Number of axioms: " + ontology.getAxiomCount());

    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, new SimpleConfiguration());
    reasoner.precomputeInferences();
    boolean consistent = reasoner.isConsistent();
    System.out.println("Consistent: " + consistent);
    Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
    Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
    if (!unsatisfiable.isEmpty()) {
      System.out.println("The following classes are unsatisfiable: ");
      for (OWLClass cls : unsatisfiable) {
        System.out.println(" " + cls);
      }
    } else {
      System.out.println("There are no unsatisfiable classes");
    }


    SQWRLQueryEngine qE= SWRLAPIFactory.createSQWRLQueryEngine(ontology);
    try {
      SQWRLResult rslt=qE.runSQWRLQuery("Q1","#Person(#John)^ #speaksLanguage(#John,?x)->sqwrl:select(?x)");
      while(rslt.next()){
        System.out.println("Name: "+rslt.getNamedIndividual("x"));
      }
    } catch (SWRLParseException e) {
      e.printStackTrace();
    }

  //manager.saveOntology(ontology, owlxmlFormat, IRI.create(fileformated.toURI()));

}
}
