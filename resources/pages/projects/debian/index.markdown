---
title: Debian repository
comments: 1
---

For some selected packages ([syslog-ng][sng] 3.3 & 3.4, and
[Zorp GPL][zorp]), I provide up-to-date packages for a number of
[Debian][debian] and [Ubuntu][ubuntu] releases - see the
<kbd>sources.list</kbd> assemby area just below. The packages will be
kept up to date for all platforms, until at least after a month of the
platforms end of life.

To see what changed between versions, please see either the
[syslog-ng 3.3 changelog][1] or the [syslog-ng 3.4 changelog][2]. For
Zorp, there is no changelog available online yet.

 [sng]: https://www.balabit.com/network-security/syslog-ng/opensource-logging-system/overview
 [zorp]: https://www.balabit.com/network-security/zorp-gpl/overview
 [debian]: http://www.debian.org/
 [ubuntu]: http://www.ubuntu.com/

 [1]: https://git.madhouse-project.org/debian/syslog-ng/plain/debian/changelog?h=packaging/debian/autobuilt/3.3
 [2]: https://git.madhouse-project.org/debian/syslog-ng/plain/debian/changelog?h=packaging/debian/autobuilt/3.4

The repository is signed by the GPG key
[6CE70C6E8B9E0644][archive-key], which one can add to apt with the following command:


    curl https://packages.madhouse-project.org/debian/archive-key.txt | sudo apt-key add -

 [archive-key]: https://packages.madhouse-project.org/debian/archive-key.txt

Packages are available for the **i386** and **amd64** architectures on
both Debian and Ubuntu, but in addition to those, the Debian
repositories also contain *syslog-ng* packages for **powerpc** and
**kfreebsd-amd64** architectures. If there's enough interest, I can
pull in more build machines, and compile for other platforms aswell.

# Choose your components!

<form class="form-inline" id="dist-select">
 <fieldset>
  <select class="inline input-xlarge">
    <optgroup label="Debian">
     <option value="debian-squeeze">Debian 6.0 (Squeeze; stable)</option>
     <option value="debian-wheezy" selected>Debian 7.0 (Wheezy)</option>
     <option value="debian-sid">Debian unstable</option>
    </optgroup>
    <optgroup label="Ubuntu">
     <option value="ubuntu-lucid">Ubuntu 10.04 LTS (Lucid Lynx)</option>
     <option value="ubuntu-natty">Ubuntu 11.04 (Natty Narwhal)</option>
     <option value="ubuntu-oneiric">Ubuntu 11.10 (Oneiric Ocelot)</option>
     <option value="ubuntu-precise">Ubuntu 12.04 LTS (Precise Pangolin)</option>
     <option value="ubuntu-quantal">Ubuntu 12.10 (Quantal Quetzal)</option>
     <option value="ubuntu-raring">Ubuntu 13.04 (Raring Ringtail)</option>
    </optgroup>
  </select>
  <label class="checkbox inline"><input id="cb-sng" checked type="checkbox">syslog-ng</label>
  <label class="checkbox inline"><input id="cb-sng-dev" type="checkbox">syslog-ng-devel</label>
  <label class="checkbox inline"><input id="cb-zorp" checked type="checkbox">zorp</label>
 </fieldset>
</form>

    deb       https://packages.madhouse-project.org/debian   wheezy   syslog-ng zorp
    deb-src   https://packages.madhouse-project.org/debian   wheezy   syslog-ng zorp
    
    deb       https://packages.madhouse-project.org/zorp-kernel   kernel   2.6

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script src="/assets/asylum/js/sources.list.js" type="text/javascript"></script>

Note that selecting **both** *syslog-ng* and *syslog-ng-devel* makes
little sense, as the packages in the latter replace the ones in the
former. So unless you are really sure you want both - because, say you
might wish to switch between the two -, choose only one.
