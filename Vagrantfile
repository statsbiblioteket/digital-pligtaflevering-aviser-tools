# -*- mode: ruby -*-
# vi: set ft=ruby :

#
# Please see vagrant/README.md
#

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  # Note:  This is not the officially recommended box!  See https://www.vagrantup.com/docs/boxes.html
  #config.vm.box = "ubuntu/trusty64"
  config.vm.box = "bento/ubuntu-14.04"
  # Initial experiments with xenial64 showed that the image is not completely ready yet.
  # config.vm.box = "ubuntu/xenial64"

  config.vm.provision :shell, :name => "bootstrap.sh", :path => "vagrant/scripts/bootstrap.sh"
  # Tomcat med DOMS + Central Web-service.
  config.vm.network :forwarded_port, :host => 7880, :guest => 7880
  # Java debug port (easy with Tomcat)
  config.vm.network :forwarded_port, :host => 8000, :guest => 8000
  # Postgresql network port, high end to avoid clash with host install
  config.vm.network :forwarded_port, :host => 15432, :guest => 5432
  # DOMS Wui Tomcat port
  config.vm.network :forwarded_port, :host => 58708, :guest => 58708
  # SBOI Tomcat port
  config.vm.network :forwarded_port, :host => 58608, :guest => 58608
  # Zookeeper lock server
  config.vm.network :forwarded_port, :host => 2181, :guest => 2181
  # Bitrepository_stub file server
  config.vm.network :forwarded_port, :host => 58709, :guest => 58709

  # ActiveMQ port
  config.vm.network :forwarded_port, :host => 61616, :guest => 61616

  # Bit repository web client port
  config.vm.network :forwarded_port, :host => 18080, :guest => 8080



  # Be able to get the artifacts from host Maven build.
  config.vm.synced_folder "vagrant/scripts", "/vagrant" # so relative paths are unchanged after DPA-61.
  config.vm.synced_folder "vagrant/target/", "/target"
  config.vm.synced_folder "delivery-samples/", "/delivery-samples"

  config.vm.provider "virtualbox" do |v|
    v.memory = 8192
    v.cpus = 2 # or more for heavy load
    ## https://stackoverflow.com/a/27878224/53897
    #v.customize ["createhd",  "--filename", "m4_disk0", "--size", "1048576"] # 1 TB
    #v.customize ["storageattach", :id, "--storagectl", "SATA Controller", "--port", "1", "--type", "hdd", "--medium", "m4_disk0.vdi"]

    # Make command line shorter for newer versions of VirtualBox.
    v.customize ["modifyvm", :id, "--audio", "none"]
    # https://github.com/hashicorp/vagrant/issues/9524

  end

  # for jvisualvm
  config.ssh.forward_x11 = true

  # Ubuntu box is by default without timezone which success in date strings which cause our parsing to fail.
  # Work around by setting the timezone of the box.
  if Vagrant.has_plugin?("vagrant-timezone")
    config.timezone.value = :host
  end
end
