#ifndef __MAFKA_SINGLETON_H__
#define __MAFKA_SINGLETON_H__

namespace mafka
{


template <typename T>
class Singleton
{
public:
	Singleton()
	{
	    ms_singleton() = static_cast< T* >( this );
	}

	~Singleton()
	{
		ms_singleton() = 0;
	}

	static T& GetSingleton()
	{
		return ( *ms_singleton() );
	}

	static T* GetSingletonPtr()
	{
		return ( ms_singleton() );
	}

private:
	static T* & ms_singleton()
	{
		static T* ms_singleton_ =0 ;
		return ms_singleton_;
	}
};

        

}

#endif //__MAFKA_SINGLETON_H__

