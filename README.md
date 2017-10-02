# botiful soup

a sort of
[primordial ooze](https://en.wikipedia.org/wiki/Primordial_soup)
for programs about natural language.

![swamp thing](doc/img/swamp_thing_00.jpg)

## `mibot`

variously, "mission impossible", "my introduction", or
"message instantly", this
[chatbot](https://en.wikipedia.org/wiki/Chatbot)
answers questions using an unannotated corpus of data,
namely, a list of answers, each with several questions that the
given answer might correspond to.  in this sense, it's more
about categorization than
[question answering](https://en.wikipedia.org/wiki/Question_answering).

the bot implementation(s) are somewhat decoupled from the
messaging services, and messaging over
[Slack](http://slack.com) is supported.

##### slack integration notes

connecting the bot to Slack requires appropriate permissions
in a workspace.
the email address that is used to
[create a workspace](http://slack.com/create)
will have appropriate permissions.

after logging in to a workspace with appropriate permissions,
[create a bot user](https://my.slack.com/services/new/bot),
copying down the API token before saving.

to add a bot to a channel, `@botname` in that channel.
to remove, `/kick @botname`.

for dev and deploy, fill in the config files
```
mibot/resources/config/dev.edn.example
mibot/test/clj/mibot/slack_test_fixture.edn.example
```
with channel names and bot tokens

### repl usage

the program is designed to be used from
[Clojure](http://clojure.org)
repl.

before firing up the repl, there's a a small amount of "glue"
to Java libraries that need be compiled, so run

```shell
mibot$ lein with-profile base javac
```

first and every time `src/java` changes.

then run `lein repl` to start the repl, and from there

* `(go :some-bot)` and `(stop)` will start and stop a
  bot called `:some-bot`, provided `:some-bot` is present in
  _both_ `config/dev.edn` and `(keys user/bot-constructors)`
* `(run-all-tests)` will stop the currently running bot,
  make certain the repl has the current state of the source tree
  loaded, and (of course), run all the test files.

##### status

the Slack integration is currently the only halfway worthwhile thing
about this portion of the repository.
few of the half-formed ideas floating around the source tree have
coalesced into chatbot form.
also, as you've probably already gathered from this readme,
the codebase is somewhat lacking in
[design stamina](https://www.martinfowler.com/bliki/DesignStaminaHypothesis.html)
due to a prioritization of fiddling with the bot implementation
over clean code in all namespaces.