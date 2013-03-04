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
            console.log(data);
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
            if (data.release == "natty" ||
                data.release == "lucid") {
                if (data.components.indexOf ("syslog-ng-devel") != -1) {
                    data.components.splice(data.components.indexOf ("syslog-ng-devel"), 1);
                }
            }
            if (data.release == "raring") {
                if (data.components.indexOf ("zorp") != -1) {
                    data.components.splice(data.components.indexOf ("zorp"), 1);
                }
            }
            return data;
        }

        function reset_controls(data) {
            if (data.release == "natty" ||
                data.release == "lucid") {
                $("#cb-sng-dev").attr("disabled", true);
            } else {
                $("#cb-sng-dev").removeAttr("disabled");
            }
            if (data.release == "raring") {
                $("#cb-zorp").attr("disabled", true);
            } else {
                $("#cb-zorp").removeAttr("disabled");
            }
        }


        $("#dist-select").change (
            function () {
                var s = get_data_from_distrib_form();
                reset_controls(s);
                redraw_sources_list(validate_selection(s));
            });
    });
