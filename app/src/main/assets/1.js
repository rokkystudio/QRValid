(() =>
{
    var t =
    {
        709: () =>
        {
            !(function ()
            {
                "use strict";
                window.APP_HELPERS =
                {
                    getCovidAppConfig: function () {
                        return fetch("/covid-web/config.json", {
                            method: "GET",
                            credentials: "include",
                        }).then(function (t) {
                            return t.json();
                        });
                    },
                };
            })();
        },
    },
    e = {};

  function a(n) {
        var r = e[n];
        if (void 0 !== r) return r.exports;
        var i = (e[n] = {
            exports: {},
        });
        return t[n](i, i.exports, a), i.exports;
    }
    (a.n = (t) => {
        var e = t && t.__esModule ? () => t.default : () => t;
        return (
            a.d(e, {
                a: e,
            }),
            e
        );
    }),
        (a.d = (t, e) => {
            for (var n in e)
                a.o(e, n) &&
                    !a.o(t, n) &&
                    Object.defineProperty(t, n, {
                        enumerable: !0,
                        get: e[n],
                    });
        }),
        (a.o = (t, e) => Object.prototype.hasOwnProperty.call(t, e)),
        (() => {
            "use strict";
            a(709),
                (window.APP = {
                    lang: "ru",
                    toogleLang: function () {
                        this.lang = "en" === this.lang ? "ru" : "en";
                        var t = new URLSearchParams(window.location.search);
                        t.set("lang", this.lang), window.history.replaceState(null, null, "?" + t.toString()), this.init();
                    },
                    getValue: function (t, e) {
                        return t ? t[e] : this[e];
                    },
                    setContainerImage: function (t) {
                        var e = "",
                            a = this;
                        return t.invalid ? ((e = "invalid"), (a.certStatusName = "Не действителен"), (a.encertStatusName = "Invalid")) : ((e = "complete"), (a.certStatusName = "Действителен"), (a.encertStatusName = "Valid")), e;
                    },
                    filterAttrs: function (t, e) {
                        return t.attrs.filter(function (t) {
                            return -1 !== e.indexOf(t.type) && t.value;
                        });
                    },

                    showAttrs: function (t)
                    {
                        var e = document.querySelector(".person-data-attrs");
                        e.innerHTML = "";

                        t.invalid
                            ? e.classList.add("hide")
                            : "ru" === this.lang
                            ? ((e.innerHTML += `<div class="mb-4 person-data-wrap attr-wrap"><div class="attrValue title-h6 bold text-center">${this.getValue(t, "fio")}</div></div>`),
                              (e.innerHTML += `<div class="mb-4 person-data-wrap attr-wrap"><div class="small-text mb-4 mr-4 attr-title">Паспорт: </div><div class="attrValue small-text gray">${a.getValue(t, "doc")}</div></div>`),
                              (e.innerHTML += `<div class="mb-4 person-data-wrap attr-wrap"><div class="small-text mb-4 mr-4 attr-title">Дата рождения: </div><div class="attrValue small-text gray">${a.getValue(
                                  t,
                                  "birthdate"
                              )}</div></div>`))
                            : "en" === a.lang &&
                              ((e.innerHTML += `<div class="mb-4 person-data-wrap attr-wrap"><div class="attrValue title-h6 bold text-center">${a.getValue(t, "enFio")}</div></div>`),
                              (e.innerHTML += `<div class="mb-4 person-data-wrap attr-wrap"><div class="small-text mb-4 mr-4 attr-title">Рassport (ID number): </div><div class="attrValue small-text gray">${
                                  a.getValue(t, "doc") || "Not specified"
                              }</div></div>`),
                              (e.innerHTML += `<div class="mb-4 person-data-wrap attr-wrap"><div class="small-text mb-4 mr-4 attr-title">International passport (ID number): </div><div class="attrValue small-text gray">${
                                  a.getValue(t, "enDoc") || "Not specified"
                              }</div></div>`),
                              (e.innerHTML += `<div class="mb-4 person-data-wrap attr-wrap"><div class="small-text mb-4 mr-4 attr-title">Date of birth: </div><div class="attrValue small-text gray">${a.getValue(
                                  t,
                                  "birthdate"
                              )}</div></div>`));
                    },
                    getParam: function (t) {
                        var e = window.location.search;
                        return new URLSearchParams(e).get(t);
                    },
                    fadeOutEffect(t) {
                        const e = setInterval(() => {
                            t && !t.style.opacity && (t.style.opacity = "1"), t && parseFloat(t.style.opacity) > 0 ? (t.style.opacity = parseFloat(t.style.opacity) - 0.5 + "") : t && (clearInterval(e), t.parentNode.removeChild(t));
                        }, 10);
                    },
                    init: function () {
                        document.body.classList.add("loading");
                        var t = this,
                            e = window.location.pathname
                                .split("/")
                                .filter((t) => !!t)
                                .pop(),
                            a = window.location.pathname.indexOf("/unrz/") > -1,
                            n = t.config.vaccineUrl + "cert/verify/" + (a ? "/unrz/" : "") + e,
                            r = this.getParam("lang");
                        function i(e) {
                            var a = e;
                            (t.cert = a), document.body.classList.remove("loading"), t.fadeOutEffect(document.getElementById("start-app-loader"));
                            var n = document.querySelector(".unrz"),
                                r = document.querySelector(".num-symbol");
                            t.showAttrs(a),
                                t.setContainerImage(a) && document.querySelector(".status-container").classList.add(t.setContainerImage(a)),
                                a.unrz ? (n.innerHTML = a.unrz) : (n.classList.add("hide"), r.classList.add("hide")),
                                t.setText(a);
                        }
                        (this.lang = r || "ru"),
                            r && (n += `?lang=${r}`),
                            t.cert
                                ? i(t.cert)
                                : fetch(n, {
                                      method: "GET",
                                      credentials: "include",
                                  })
                                      .then(function (t) {
                                          return t.json();
                                      })
                                      .then(
                                          function (e) {
                                              i(e || t.emptyState());
                                          },
                                          function () {
                                              document.body.classList.remove("loading"), i(t.emptyState());
                                          }
                                      );
                    },
                    getConfig: function () {
                        return window.APP_HELPERS.getCovidAppConfig().then(
                            function (t) {
                                return (this.config = t), !0;
                            }.bind(this)
                        );
                    },
                    emptyState: function () {
                        return {
                            title: "Сертификат о вакцинации COVID-19",
                            entitle: "Certificate of COVID-19 Vaccination",
                            invalid: "Не действителен",
                            eninvalid: "Invalid",
                        };
                    },
                    setText: function (t) {
                        var e = document.querySelector(".lang-image");
                        if (
                            ((document.querySelector(".main-title").innerHTML = this.getValue(this.emptyState(), ("ru" === this.lang ? "" : "en") + "title")),
                            (document.querySelector(".button").innerHTML = "ru" === this.lang ? "Закрыть" : "Close"),
                            (document.querySelector(".lang").innerHTML = "ru" === this.lang ? "RUS" : "ENG"),
                            e.classList.remove("ru", "en"),
                            e.classList.toggle(this.lang),
                            t.invalid)
                        ) {
                            var a = document.querySelector(".not-found");
                            a.classList.remove("hide"), (a.innerHTML = this.getValue("", ("en" === this.lang ? "en" : "") + "certStatusName"));
                        } else {
                            var n = document.querySelector(".cert-name");
                            n.classList.remove("hide"), (n.innerHTML = this.getValue("", ("en" === this.lang ? "en" : "") + "certStatusName"));
                        }
                    },
                }),
                APP.getConfig().then(function () {
                    APP.init();
                });
        })();
})();
