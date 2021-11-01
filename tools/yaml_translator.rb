#!/usr/bin/env ruby

if ARGV.size != 2
  puts "Usage: #{$0} <from_language> <to_language>"
  exit -1
end

require 'rubygems'
require 'yaml'
require 'json'
require 'uri'
require 'tr4n5l4te' # gem 'tr4n5l4te'

@translator = Tr4n5l4te::Translator.new

def translate(string)
  @translator.translate(string, @from_language, @to_language)
end

def process(hash)
  hash.inject({}) do |h, pair|
    key, value = pair
    h[key] = value.kind_of?(Hash) ? process(value) : translate(value)
    h
  end
end

@from_language = ARGV[0].to_sym
@to_language = ARGV[1].to_sym

hash = YAML.load_file("#{@from_language}.yml")

File.open("#{@to_language}.yml", 'w') do |out|
  YAML.dump(process(hash), out)
end