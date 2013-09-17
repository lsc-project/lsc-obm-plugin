#=================================================
# Specification file for LSC-project OBM plugin
#
# Install LSC OBM plugin
#
# BSD License
#
# Copyright (c) 2009 - 2013 LSC Project
#=================================================

#=================================================
# Variables
#=================================================
%define lsc_obm_name	lsc-obm-plugin
%define lsc_obm_version	1.0
%define lsc_min_version	2.0.3
%define lsc_user        lsc
%define lsc_group       lsc

#=================================================
# Header
#=================================================
Summary: LSC OBM plugin
Name: %{lsc_obm_name}
Version: %{lsc_obm_version}
Release: 0%{?dist}
License: BSD
BuildArch: noarch

Group: Applications/System
URL: http://lsc-project.org

Source: %{lsc_obm_name}-%{lsc_obm_version}-distribution.jar
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

Requires(pre): coreutils
Requires: lsc >= %{lsc_min_version}

%description
This is an OBM plugin for LSC

%prep

%build

%install

rm -rf %{buildroot}

# Create directories
mkdir -p %{buildroot}/usr/%{_lib}/lsc

# Copy files
cp -a %{SOURCE0} %{buildroot}/usr/%{_lib}/lsc

%post

/bin/chown -R %{lsc_user}:%{lsc_group} /usr/%{_lib}/lsc 


%postun

%clean
rm -rf %{buildroot}

%files
%defattr(-, root, root, 0755)
/usr/%{_lib}/lsc/lsc-obm-plugin*

#=================================================
# Changelog
#=================================================
%changelog
* Thu Sep 16 2013 - Clement Oudot <clem@lsc-project.org> - 1.0-0
- First package for LSC OBM plugin
