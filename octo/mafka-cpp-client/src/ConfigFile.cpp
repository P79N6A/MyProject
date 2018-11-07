#include "ConfigFile.h"
#include "Common.h"
#include "StringConverter.h"
#include "StringUtil.h"

#include <fstream>

namespace mafka
{

ConfigSection::ConfigSection()
{

}

ConfigSection::~ConfigSection()
{

}

bool ConfigSection::GetInteger(std::string const& key, int& value) const
{
	Properties::const_iterator i = m_properties.find(key);
	if(i == m_properties.end())
	{
		return false;
	}

	value = StringConverter::ParseInt(i->second);

	return true;
}

bool ConfigSection::GetInteger(std::string const& key, size_t& value) const
{
	Properties::const_iterator i = m_properties.find(key);
	if(i == m_properties.end())
	{
		return false;
	}

	value = StringConverter::ParseInt(i->second);

	return true;
}

bool ConfigSection::GetFloat(std::string const& key, double& value)
{
	Properties::const_iterator i = m_properties.find(key);
	if(i == m_properties.end())
	{
		return false;
	}

	value = StringConverter::ParseFloat(i->second);

	return true;
}

bool ConfigSection::GetString(std::string const& key, std::string& value) const
{
	Properties::const_iterator i = m_properties.find(key);
	if(i == m_properties.end())
	{
		return false;
	}

	value = i->second;

	return true;
}

bool ConfigSection::GetBool(std::string const& key, bool& value) const
{
	Properties::const_iterator i = m_properties.find(key);
	if(i == m_properties.end())
	{
		return false;
	}

	value = StringConverter::ParseInt(i->second) > 0;

	return true;
}

bool ConfigSection::IsEmpty() const
{
	return m_properties.empty();
}

void ConfigSection::Insert(std::string const& key, std::string const& value)
{
	m_properties.insert(Properties::value_type(key, value));
}

void ConfigSection::GetAll(Properties& properties) const
{
	properties = m_properties;
}

void ConfigSection::SetAll(Properties const& properties)
{
	m_properties = properties;
}

ConfigFile::ConfigFile()
{

}

ConfigFile::~ConfigFile()
{
	for (SectionMap::iterator i = m_sections.begin(); i != m_sections.end(); ++i)
	{
		delete (i->second);
	}
	m_sections.clear();
}

bool ConfigFile::LoadFromFile(std::string const& filename)
{
	if(!m_sections.empty())
	{
		this->~ConfigFile();
	}

	std::ifstream stream;
	stream.open(filename.c_str());
	if (!stream)
	{
		return false;
	}

	std::string line;
	ConfigSection* cur_settings = new ConfigSection;
	m_sections.insert(SectionMap::value_type(StringUtil::EMPTY_STRING, cur_settings));
    while (std::getline(stream,line))
    {
		StringUtil::TrimString(line);
        if (line.length() > 0 && line[0] != '#')
        {
            if (line[0] == '[' && line[line.length()-1] == ']')
            {
                // Section
                std::string section = line.substr(1, line.length() - 2);
				SectionMap::const_iterator seci = m_sections.find(section);
				if (seci == m_sections.end())
				{
					cur_settings = new ConfigSection;
					m_sections.insert(SectionMap::value_type(section, cur_settings));
				}
				else
				{
					cur_settings = seci->second;
				} 
            }
            else
            {
				std::string const separators("=");
				std::string::size_type separator_pos = line.find_first_of(separators, 0);
                if (separator_pos != std::string::npos)
                {
                    std::string key = line.substr(0, separator_pos);
					std::string::size_type nonseparator_pos = line.find_first_not_of(separators, separator_pos);
                    std::string value = (nonseparator_pos == std::string::npos) ? "" : line.substr(nonseparator_pos);
                    StringUtil::TrimString(key);
                    StringUtil::TrimString(value);

                    cur_settings->Insert(key, value);
                }
            }
        }
    }

	return true;
}

bool ConfigFile::SaveToFile(std::string const& filename)
{
	std::ofstream stream;
	stream.open(filename.c_str(), std::ios_base::out | std::ios_base::trunc);
	if (!stream)
	{
		return false;
	}

	for(SectionMap::const_iterator i = m_sections.begin(); i != m_sections.end(); ++i)
	{
		ConfigSection* section = i->second;
		stream << '[' << i->first << ']' << std::endl;
		if(!stream)
		{
			return false;
		}
		ConfigSection::Properties properties;
		section->GetAll(properties);
		for(ConfigSection::Properties::const_iterator j = properties.begin(); j != properties.end(); ++j)
		{
			stream << j->first << '=' << j->second << std::endl;
			if(!stream)
			{
				return false;
			}
		}
	}

	return true;
}


bool ConfigFile::HasSection(std::string const& section) const
{
	SectionMap::const_iterator i = m_sections.find(section);
	return i != m_sections.end();
}

ConfigSection const& ConfigFile::GetSetion(std::string const& section) const
{
	SectionMap::const_iterator i = m_sections.find(section);
	if(i == m_sections.end())
	{
		static ConfigSection none;
		return none;
	}

	return *(i->second);
}

void ConfigFile::SetSection(std::string const& section, ConfigSection::Properties const& properties)
{
	ConfigSection* cur = NULL;
	SectionMap::const_iterator i = m_sections.find(section);
	if(i == m_sections.end())
	{
		cur = new ConfigSection;
		m_sections.insert(SectionMap::value_type(section, cur));
	}
	else
	{
		cur = i->second;
	}

	cur->SetAll(properties);
}


}
