/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iiit.chatbot.service.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.ResourceUtils;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class OpenNLPModelFactory {

	// Models to be loaded in memory
    private SentenceModel sentenceModel;
    private TokenizerModel tokenizerModel;
    private POSModel partOfSpeechModel;
    private ChunkerModel chunkerModel;
    // custom models
    private TokenNameFinderModel actionFinderModel;
    private TokenNameFinderModel itemFinderModel;
    private ParserModel parserModel;

    private Parser statementParser = null;

    //
    private ChunkerME phraseChunker;
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME partOfSpeechTagger;

    // custom NER
    private NameFinderME actionFinder;
    private NameFinderME itemFinder;

    private String root = null;


    public OpenNLPModelFactory(){
    	 File file = null;;
		try {
			file = ResourceUtils.getFile("classpath:nlpmodel/");
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("Resource Not Found");
		}
	        System.out.println(file);
	        root=  file.getPath() + File.separator;
	        System.out.println(root);
    	try{
	        initModels();
	        tokenizer = new TokenizerME(tokenizerModel);
	        partOfSpeechTagger = new POSTaggerME(partOfSpeechModel);
	        sentenceDetector = new SentenceDetectorME(sentenceModel);
	        phraseChunker = new ChunkerME(chunkerModel);
	        actionFinder = new NameFinderME(actionFinderModel);
	        itemFinder = new NameFinderME(itemFinderModel);
	        System.out.println("Model Loading Successful");

    	}catch(IOException ex){
    		System.out.println("I/O Error occured");
    	}
    }


    public OpenNLPModelFactory(String test) throws IOException {
    	InputStream sentenceModelStream = getInputStream("models/en-sent.bin");
        InputStream tokenizereModelStream = getInputStream("models/en-token.bin");
        InputStream partOfSpeechModelStream = getInputStream("models/en-pos-maxent.bin");


        sentenceModel = new SentenceModel(sentenceModelStream);;
        tokenizerModel = new TokenizerModel(tokenizereModelStream);
        partOfSpeechModel = new POSModel(partOfSpeechModelStream);

    	tokenizer = new TokenizerME(tokenizerModel);
    	partOfSpeechTagger = new POSTaggerME(partOfSpeechModel);
        sentenceDetector = new SentenceDetectorME(sentenceModel);
    }

    //private static String root = "resources/";

    private void initModels() throws IOException{
    		InputStream sentenceModelStream = getInputStream(root + "models/en-sent.bin");
            InputStream tokenizereModelStream = getInputStream(root + "models/en-token.bin");
            InputStream partOfSpeechModelStream = getInputStream(root + "models/en-pos-maxent.bin");
            InputStream chunkerrModelStream =  getInputStream(root+"models/en-chunker.bin");
            InputStream actionFinderModelStream =  getInputStream(root + "actionmodel/supplyChainAction.bin");
            InputStream itemFinderModelStream =  getInputStream(root + "itemmodel/itemmodel.bin");
            InputStream parserModelIStream = getInputStream(root + "models/en-parser-chunking.bin");

            sentenceModel = new SentenceModel(sentenceModelStream);;
            tokenizerModel = new TokenizerModel(tokenizereModelStream);
            partOfSpeechModel = new POSModel(partOfSpeechModelStream);
            chunkerModel = new ChunkerModel(chunkerrModelStream);
            actionFinderModel = new TokenNameFinderModel(actionFinderModelStream);
            itemFinderModel = new TokenNameFinderModel(itemFinderModelStream);
            parserModel = new ParserModel(parserModelIStream);
    		statementParser = ParserFactory.create(parserModel);

    }

    @SuppressWarnings("resource")
	private InputStream getInputStream(String resource) throws FileNotFoundException  {
    	InputStream inputS = null;
    	try {
			inputS = new FileInputStream(new File(resource));
		} catch (Exception e) {
			// For Jars in SOLR
			e.printStackTrace();
			System.out.println("Inside the Exception =" + "/" + resource);
			inputS = getClass().getResourceAsStream("/" + resource);
		}
        return inputS;
    }

    public String[] segmentSentences(String document) {
        return sentenceDetector.sentDetect(document);
    }

    public String[] tokenizeSentence(String sentence) {
        return tokenizer.tokenize(sentence);
    }

    public Span[] findAction(String[] tokens) {
        return actionFinder.find(tokens);
    }

    public double[] findActionProb(Span[] spans) {
        return actionFinder.probs(spans);
    }

    public Span[] findItem(String[] tokens) {
        return itemFinder.find(tokens);
    }

    public double[] findItemProb(Span[] spans) {
        return itemFinder.probs(spans);
    }

    public String[] tagPartOfSpeech(String[] tokens) {
        return partOfSpeechTagger.tag(tokens);
    }

    public double[] getPartOfSpeechProbabilities() {
        return partOfSpeechTagger.probs();
    }

    public String[] getChunkedPhrases(String[] tokens, String[] tags) {
    	return phraseChunker.chunk(tokens, tags);
    }


    public Span[] getChunkedSpan(String[] tokens, String[] tags) {
    	return phraseChunker.chunkAsSpans(tokens, tags);
    }

    public String[] getChunkedPhrases(String sentence) {
    	String[] tokens = tokenizeSentence(sentence);
        String[] tags = tagPartOfSpeech(tokens);
        return getChunkedPhrases(tokens, tags);
    }

    public String posValue(String k) {
    	String value = k;
    	switch(k) {
    	 case "CC": value = "Coordinating conjunction";break;
    	 case "CD": value = "Cardinal number";break;
    	 case "DT": value = "Determiner";break;
    	 case "EX": value = "Existential there";break;
    	 case "FW": value = "Foreign word";break;
    	 case "IN": value = "Preposition or subordinating conjunction";break;
    	 case "JJ": value = "Adjective";break;
    	 case "JJR": value = "Adjective, comparative";break;
    	 case "JJS": value = "Adjective, superlative";break;
    	 case "LS": value = "List item marker";break;
    	 case "MD": value = "Modal";break;
    	 case "NN": value = "Noun, singular or mass";break;
    	 case "NNS": value = "Noun, plural";break;
    	 case "NNP": value = "Proper noun, singular";break;
    	 case "NNPS": value = "Proper noun, plural";break;
    	 case "PDT": value = "Predeterminer";break;
    	 case "POS": value = "Possessive ending";break;
    	 case "PRP": value = "Personal pronoun";break;
    	 case "PRP$": value = "Possessive pronoun";break;
    	 case "RB": value = "Adverb";break;
    	 case "RBR": value = "Adverb, comparative";break;
    	 case "RBS": value = "Adverb, superlative";break;
    	 case "RP": value = "Particle";break;
    	 case "SYM": value = "Symbol";break;
    	 case "TO": value = "to";break;
    	 case "UH": value = "Interjection";break;
    	 case "VB": value = "Verb, base form";break;
    	 case "VBD": value = "Verb, past tense";break;
    	 case "VBG": value = "Verb, gerund or present participle";break;
    	 case "VBN": value = "Verb, past participle";break;
    	 case "VBP": value = "Verb, non-3rd person singular present";break;
    	 case "VBZ": value = "Verb, 3rd person singular present";break;
    	 case "WDT": value = "Wh-determiner";break;
    	 case "WP": value = "Wh-pronoun";break;
    	 case "WP$": value = "Possessive wh-pronoun";break;
    	 case "WRB": value = "Wh-adverb";break;
    	 default: break;
    	}
    	return value;
    }

    public float posScore(String k) {
    	float value = 1;
    	switch(k) {
    	 case "JJ":
    	 case "JJR":
    	 case "JJS": value = 2;break;
    	 case "NN":
    	 case "NNS":
    	 case "NNP":
    	 case "NNPS": value = 20;break;
    	 case "VB":
    	 case "VBD":
    	 case "VBG":
    	 case "VBN":
    	 case "VBP":
    	 case "VBZ": value = 4;break;
    	 default: break;
    	}
    	return value;
    }


	public Parser getStatementParser() {
		return statementParser;
	}
	public ParserModel getParserModel() {
		return parserModel;
	}
	public void setParserModel(ParserModel parserModel) {
		this.parserModel = parserModel;
	}
}