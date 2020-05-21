package issue.post.form.netty

import groovy.transform.EqualsAndHashCode
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Flowable
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class HttpPostSpec extends Specification {

    @Shared
    @AutoCleanup
    ApplicationContext context = ApplicationContext.run()

    @Shared
    EmbeddedServer embeddedServer = context.getBean(EmbeddedServer).start()

    @Shared
    @AutoCleanup
    HttpClient client = context.createBean(HttpClient, embeddedServer.getURL())

    void "test simple post request with Form data"() {
        given:
        def book = new Book(title: "The Stand", pages: 1000)
        when:
        Flowable<HttpResponse<Book>> flowable = Flowable.fromPublisher(client.exchange(
            HttpRequest.POST("/post/form", book)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-My-Header", "Foo"),

            Book
        ))
        HttpResponse<Book> response = flowable.blockingFirst()
        Optional<Book> body = response.getBody()

        then:
        response.status == HttpStatus.OK
        response.contentType.get() == MediaType.APPLICATION_JSON_TYPE
        response.contentLength == 34
        body.isPresent()
        body.get() instanceof Book
        body.get().title == 'The Stand'
    }

    @Controller('/post')
    static class PostController {

        @Post(uri = '/form', consumes = MediaType.APPLICATION_FORM_URLENCODED)
        Book form(@Body Book book, @Header String contentType, @Header long contentLength, @Header accept, @Header('X-My-Header') custom) {
            println "=" * 100
            println "book.title = " + book.title
            println "=" * 100
            assert contentType == MediaType.APPLICATION_FORM_URLENCODED
            assert contentLength == 26
            assert accept == MediaType.APPLICATION_JSON
            assert custom == 'Foo'
            return book
        }
    }

    @EqualsAndHashCode
    static class Book {
        String title
        Integer pages
    }

}
