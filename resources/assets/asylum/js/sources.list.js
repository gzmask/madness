$(document).ready(
    function () {
        function get_data_from_distrib_form () {
            var distrel = $("#distro-select").val().split("-");
            var components = [];
            var zorp_extra = false;

            var sng = $("#sng-select").val();
            if (sng != "syslog-ng-none") {
                components.push(sng);
            }

            var zorp = $("#zorp-select").val();
            if (zorp != "zorp-none") {
                components.push("zorp");
                zorp_extra = true;
            }

            return {
                "dist": distrel[0],
                "release": distrel[1],
                "components": components,
                "zorp_extra": zorp_extra,
            }
        }

        function redraw_sources_list(data) {
            var new_text = 
                "deb       http://packages.madhouse-project.org/" + data.dist + "   " + data.release + "   " + data.components.join(" ") + "\n" +
                "deb-src   http://packages.madhouse-project.org/" + data.dist + "   " + data.release + "   " + data.components.join(" ") + "\n";
            if (data.zorp_extra) {
                new_text += "\ndeb       http://packages.madhouse-project.org/zorp-kernel   kernel   2.6";
            } else {
                new_text += "\n\n";
            }
            $("pre code:last").fadeOut(400, 
                                        function () {
                                            $("pre code:last").text(new_text);
                                            $("pre code:last").fadeIn();
                                        });
        }

        function validate_selection(data) {
            var alert_text = null;

            if (data.release == "natty" ||
                data.release == "lucid") {
                if (data.components.indexOf ("syslog-ng-devel") != -1 ||
                    data.components.indexOf ("syslog-ng-3.4") != -1) {
                    data.components.splice(data.components.indexOf ("syslog-ng-devel"), 1);
                    alert_text = "The selected distribution does not have packages for syslog-ng-3.4 or syslog-ng-devel.";
                }
            }
            if (data.release == "raring") {
                if (data.components.indexOf ("zorp") != -1) {
                    data.components.splice(data.components.indexOf ("zorp"), 1);
                    alert_text = "The selected distribution does not have packages for zorp."
                }
            }

            if (alert_text) {
                $("#alert-box").text(alert_text).fadeIn();
            } else {
                $("#alert-box").fadeOut();
            }
            return data;
        }

        function reset_controls(data) {
            $("#sng-select").find("option")
                .each(function (x) {
                          if (this.value == "syslog-ng-devel" ||
                              this.value == "syslog-ng-3.4") {
                              if (data.release == "natty" ||
                                  data.release == "lucid") {
                                  $(this).attr("disabled", true);
                              } else {
                                  $(this).removeAttr("disabled");
                              }
                          }
                      });

            $("#zorp-select").find("option")
                .each(function (x) {
                          if (this.value == "zorp") {
                              if (data.release == "raring") {
                                  $(this).attr("disabled", true);
                              } else {
                                  $(this).removeAttr("disabled")
                              }
                          }
                      });
        }

        $("#dist-select").change (
            function () {
                var s = get_data_from_distrib_form();
                reset_controls(s);
                redraw_sources_list(validate_selection(s));
            });
    });
