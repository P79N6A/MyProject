#ifndef __MAFKA_CONFIG_FILE_H__
#define __MAFKA_CONFIG_FILE_H__

#include <string>
#include <map>

namespace mafka
{

class ConfigSection
{
public:
	typedef std::map<std::string, std::string> Properties;

public:
	ConfigSection();
	~ConfigSection();

public:
	bool GetInteger(std::string const& key, int& value) const;
	bool GetInteger(std::string const& key, size_t& value) const;
	bool GetFloat(std::string const& key, double& value);
	bool GetString(std::string const& key, std::string& value) const;
	bool GetBool(std::string const& key, bool& value) const;

	bool IsEmpty() const;

public:
	void Insert(std::string const& key, std::string const& value);
	void GetAll(Properties& properties) const;
	void SetAll(Properties const& properties);

private:
	Properties m_properties;
};

class ConfigFile
{
public:
	ConfigFile();
	~ConfigFile();

public:
	bool LoadFromFile(std::string const& filename);
	bool SaveToFile(std::string const& filename);

	bool HasSection(std::string const& section) const;
	ConfigSection const& GetSetion(std::string const& section) const;
	void SetSection(std::string const& section, ConfigSection::Properties const& properties);

private:
	typedef std::map<std::string, ConfigSection*> SectionMap;
	SectionMap m_sections;
};


}

#endif // __MAFKA_CONFIG_FILE_H__