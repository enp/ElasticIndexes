#!/usr/bin/env groovy

@Grab('io.vertx:vertx-core:[3,)')
@Grab('io.vertx:vertx-lang-groovy:[3,)')
@Grab('io.vertx:vertx-web:[3,)')
@Grab('io.vertx:vertx-web-templ-handlebars:[3,)')

def vertx  = io.vertx.groovy.core.Vertx.vertx()
def router = io.vertx.groovy.ext.web.Router.router(vertx)
def engine = io.vertx.groovy.ext.web.templ.HandlebarsTemplateEngine.create()

def prefix = ''

router.get(prefix+'/indexes').handler({ context ->
  vertx.createHttpClient().get(9200, '127.0.0.1','/_aliases', { response ->
    response.bodyHandler({ body ->
      context.put("indexes", body.toJsonObject())
      engine.render(context, "indexes", { result ->
        if (result.succeeded()) {
          context.response().end(result.result())
        } else {
          context.fail(result.cause())
        }
      })
    })
    response.exceptionHandler({ error ->
      context.fail(error)
    })
  }).end()
})


router.route(prefix+'/*').handler(io.vertx.groovy.ext.web.handler.StaticHandler.create())

vertx.createHttpServer().requestHandler(router.&accept).listen(8100,'0.0.0.0')
