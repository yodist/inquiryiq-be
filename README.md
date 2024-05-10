# Inquiry IQ BE
## How to Run
### Run with Intellij IDEA
1. Install Intellij IDEA Community Edition
2. Install JDK 21 (amazon corretto version)
3. Clone this repo
4. Open this repo with Intellij IDEA
5. Go to File (left-top corner) > Settings > Plugins > search "Lombok" > Install
6. Go to File > Project Structure ... > Project Settings > Project > set SDK to Corretto 21
7. Go to your PC environment variable, add new variable -> SERPAPI_KEY with value "test"
8. Build the project with `mvn clean install`
9. Run the project with `mvn spring-boot:run`, this project will run at port 5000
10. Open postman and request to localhost:5000/api/related-question with GET method

## Useful Development Reference
### May 4th, 2024:
issue with adding google serp api to pom.xml
- https://github.com/serpapi/google-search-results-java/issues/22

please follow this for the logback in json format implementation
- https://www.baeldung.com/java-log-json-output

