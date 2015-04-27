package org.datavault.broker.bagit;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by stuartlewis on 21/04/15.
 */
public class Bagit {

    public static void main(String[] args) throws Exception {
        File directory = new File("/Users/stuartlewis/Pictures");

        BagFactory bf = new BagFactory();

        DefaultCompleter completer = new DefaultCompleter(bf);
        completer.setGenerateBagInfoTxt(true);
        completer.setUpdateBaggingDate(true);
        completer.setUpdateBagSize(true);
        completer.setUpdatePayloadOxum(true);
        completer.setGenerateTagManifest(true);
        completer.setTagManifestAlgorithm(Manifest.Algorithm.valueOfBagItAlgorithm(Manifest.Algorithm.MD5.bagItAlgorithm));
        completer.setPayloadManifestAlgorithm(Manifest.Algorithm.valueOfBagItAlgorithm(Manifest.Algorithm.MD5.bagItAlgorithm));

        FileSystemWriter fsw = new FileSystemWriter(bf);

        Bag bag = bf.createBag();

        try {
            bag.addFileToPayload(directory);


            Bag newBag = completer.complete(bag);
            try {
                newBag.write(fsw, new File("/Users/stuartlewis/test/hello.bag"));
            } finally {
                newBag.close();
            }

            System.out.print("Verifying... ");
            CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
            ParallelManifestChecksumVerifier checksumVerifier = new ParallelManifestChecksumVerifier();
            ValidVerifierImpl verifier = new ValidVerifierImpl(completeVerifier, checksumVerifier);
            SimpleResult result = verifier.verify(bag);
            System.out.println("complete");
        } finally {
            bag.close();
        }

        TikaConfig tika = new TikaConfig();
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, "/Users/stuartlewis/Dropbox/RLS Summer Update 2013v3.pdf");
        MediaType m = tika.getDetector().detect(new BufferedInputStream(new FileInputStream("/Users/stuartlewis/Dropbox/RLS Summer Update 2013v3.pdf")), metadata);
        System.out.println("File " + " is " + m.getType() + " / " + m.getSubtype() + " /" + m.toString());




    }
}
