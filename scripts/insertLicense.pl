#!/usr/bin/perl
use strict;
use warnings;
use File::Basename;
use File::Find;

if(!defined $ARGV[0] or !defined $ARGV[1] ) {
	print "Usage: insertLicense.pl <license_file_location> <files_root_directory>" and exit;
}

my $license_file =  $ARGV[0]; 
my $source_dir 	 =  $ARGV[1]; 

# Read license file
my $license_text = do { local(@ARGV, $/) = "$license_file"; <> };

# Add license to all files
find(\&add_license, $source_dir);

# Add license text from $license_file to all files within $source_dir
sub add_license {		
	# processing only files.
	return unless -f; 
	
	# Adding license to only .java and .groovy files
	my ($dir, $name, $ext) = fileparse("$_", qr/\.[^.]*/);
	return unless ($ext eq '.java' or $ext eq '.groovy');
	
	# Alert if file is not readable.
	if(!-w "$_") {
		print "WARNING: File not writeable: ". $File::Find::name ."\n";
		return;
	}
		
	# Read file contents into $curfile
	my $curfile = do { local(@ARGV, $/) = "$_"; <> };
	
	# Open file for editing
	open(FILE, ">$_") or die "FATAL: Cannot open $File::Find::name\n";					
	
	print "INFO: Adding license to $File::Find::name\n";
	# Add license or replace already existing license with the current one.
	my $license_start_index = index($curfile, "/*****");
	# Expecting it to be in the first line of the file. 
	if($license_start_index > -1 && $license_start_index < 10) {		
		my $license_end_index = index($curfile, "*****/");
		$curfile = $license_text. substr($curfile, ($license_end_index + 6));				
	} else {
		$curfile = $license_text."\n".$curfile;
	}
	
	print FILE $curfile;
	close FILE;
}