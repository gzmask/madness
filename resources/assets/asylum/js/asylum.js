$(document).ready(function () {
  $("a[rel=tooltip]").tooltip();
  $(".rel-tooltip").tooltip();
  $(".row-fluid *[rel=tooltip]").tooltip();
  $("a[href=#distro-debian-wheezy]").tab('show');
});
