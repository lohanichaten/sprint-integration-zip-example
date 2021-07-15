package learning.spring;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.zip.splitter.UnZipResultSplitter;
import org.springframework.integration.zip.transformer.UnZipTransformer;
import org.springframework.integration.zip.transformer.ZipTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.nativex.hint.TypeHint;


@TypeHint(types=GenericMessage.class)
@SpringBootApplication
public class ZipAndGraalIntegrationExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZipAndGraalIntegrationExampleApplication.class, args);
	}
	
	
	
	@Bean
	UnZipTransformer unZipTransformer() {
		return new UnZipTransformer();
	}

	
	@Bean
	UnZipResultSplitter unZipResultSplitter() {
		return new UnZipResultSplitter();
	}
	
	@Bean
	ZipTransformer zipTranformer() {
		return new ZipTransformer();
	}
	
	@Bean
	IntegrationFlow files(@Value("file:////${user.home}/Desktop/in") File in,
						  @Value("file:////${user.home}/Desktop/out") File out,
						  UnZipResultSplitter 	unZipResultSplitter,
						  ZipTransformer zipTranformer,
						  UnZipTransformer unZipTransformer
						  
			) {
		
		var inbound=Files.inboundAdapter(in).autoCreateDirectory(true);
		
		return IntegrationFlows
				.from(inbound,pm->pm.poller(p->p.fixedRate(1000)))
				.transform(unZipTransformer)
				.split(unZipResultSplitter)
				.transform(zipTranformer)
				.handle(Files.outboundAdapter(out)
							.autoCreateDirectory(true)
							.fileNameGenerator(new FileNameGenerator() {
								
								@Override
								public String generateFileName(Message<?> message) {
									return message.getHeaders().get(FileHeaders.FILENAME)+".zip";
								}
							}))
//				.handle(new MessageHandler() {
//					
//					@Override
//					public void handleMessage(Message<?> message) throws MessagingException {
//						System.out.println("new Message:"+message.getPayload().toString());
//						message.getHeaders().forEach((k,v)->System.out.println(k+"="+v));
//						
//					}
//				})
				.get();
		
		
	}
}
