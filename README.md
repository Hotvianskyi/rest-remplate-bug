# Demo for demonstrating interceptor breaking BufferingClientHttpRequestFactory
Introduction of `BufferingClientHttpRequestFactory` should disable the streaming mode and allow us to have access to the 
body after the request. However, under very specific use-case, this doesn't seem to work as intended.

`HelloWorldWithApiClientController` demonstrates the specific breaking use-case, as well as alternatives.
The use case is calling another server using restTemplate as a client, and under these requirements:
1. It must be a POST/PUT request
2. RestTemplate must include this line: `restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));` or an equivalent
3. RestTemplate must have an interceptor which tries to call getBody() on the response object inside of intercept()
4. Response from the endpoint, called by RestTemplate must be a 4xx or 5xx error (in this demo 401 is used)

the request can't proceed because of IOException
