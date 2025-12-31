# 🚀 User Management Microservice

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Security-OAuth2-blueviolet.svg)](https://spring.io/projects/spring-security)
[![Feign](https://img.shields.io/badge/HTTP--Client-Feign-blue.svg)](https://spring.io/projects/spring-cloud-openfeign)
[![Docker](https://img.shields.io/badge/Docker-Compose%20Enabled-blue.svg)](https://www.docker.com/)
[![Quality Gate Status](https://img.shields.io/badge/SonarQube-Quality%20Gate-brightgreen.svg)](https://sonarqube.org)

Este microsserviço é o componente de **Gestão de Usuários** do ecossistema **Agendador de Tarefas**. Ele atua como o provedor central de dados cadastrais e autenticação, integrando-se nativamente com um **BFF (Backend for Frontend)** e consumindo serviços externos de forma resiliente.

---

## 🏗️ Arquitetura do Sistema

O projeto foi desenhado seguindo padrões modernos de microsserviços:

* **Autenticação Robusta:** Implementação de **OAuth2**, garantindo comunicação segura entre o BFF e os serviços de domínio.
* **Integração com BFF:** Atua como o servidor de recursos para o `Bff-agendador-de-tarefas`, centralizando as operações de perfil e credenciais.
* **Consumo de APIs Externas:** Utiliza **Spring Cloud Feign** para comunicação declarativa com a API do ViaCep, garantindo um código limpo e fácil manutenção de integração de endereços.
* **Infraestrutura como Código:** Orquestração completa via **Docker Compose**, permitindo que o banco de dados PostgreSQL e o serviço subam de forma integrada.



---

## 🛠️ Stack Tecnológico

* **Core:** Java 17 & Spring Boot 3.x
* **Segurança:** Spring Security & OAuth2 (Tokens JWT)
* **Comunicação:** Spring Cloud OpenFeign (Declarative REST Client)
* **Persistência:** Spring Data JPA & PostgreSQL
* **Containers:** Docker & Docker Compose
* **Testes:** JUnit 5, Mockito & AssertJ

---

## 🌟 Diferenciais do Projeto

### 🔌 Comunicação Declarativa com Feign
Diferente do RestTemplate legível, o uso do **Feign Client** permite que a integração com o ViaCep seja feita através de interfaces, abstraindo a complexidade das chamadas HTTP e facilitando a criação de mocks para testes unitários.

### 🔄 Conversão de Dados Inteligente (Custom Converters)
A classe `UsuarioConverter` gerencia a complexidade de transformar DTOs em Entidades (e vice-versa), suportando:
* **Updates Parciais:** Atualiza apenas os campos enviados no JSON, preservando os dados existentes no banco.
* **Relacionamentos:** Mapeamento automático de listas de endereços e telefones vinculados ao usuário.

### 🐳 Prontidão para DevOps
O projeto já nasce "containerizado". O arquivo `docker-compose.yml` na raiz gerencia a dependência do banco de dados e as variáveis de rede necessárias para o funcionamento em conjunto com o Agendador.



---

## 🚀 Como Executar

1.  **Pré-requisitos:** Possuir Docker e Docker Compose instalados.
2.  **Subir o ecossistema:**
    ```bash
    docker-compose up -d
    ```
3.  **Ambiente de Desenvolvimento:**
    Caso prefira rodar localmente, os perfis do Spring estão configurados para buscar o banco de dados no `localhost` ou no container, dependendo do perfil ativo.

---

## 📊 Estratégia de Testes

O projeto prioriza a confiabilidade através de testes automatizados:
* **Unit Tests:** Cobertura total dos conversores e lógica de negócio.
* **Mocking:** Isolamento total de APIs externas através de mocks do Feign Client, garantindo que os testes não dependam da internet.
  
---

### 📊 Qualidade e Estática de Código
Para garantir a manutenibilidade e a segurança do projeto, utilizamos o **SonarQube** na análise estática do código. 
* **Foco em Clean Code:** Monitorização constante de duplicidade de código e dívida técnica.
* **Segurança:** Análise automática de vulnerabilidades em bibliotecas e padrões de implementação.
* **Complexidade:** Controlo de complexidade ciclomática para manter os métodos simples e testáveis.
---

## 🤝 Desenvolvedor

**Lucas Teixeira** - [GitHub](https://github.com/dev-lucasteixeira)

---
