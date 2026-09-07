package br.com.alura.screnmatch;

import br.com.alura.screnmatch.principal.Principal;
import br.com.alura.screnmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScrenmatchApplication  {

	public static void main(String[] args) {
		SpringApplication.run(ScrenmatchApplication.class, args);
	}

}
